package com.lpapi.solver.glpk

import com.lpapi.model.LPConstraint
import com.lpapi.model.LPModel
import com.lpapi.model.LPModelResult
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType
import com.lpapi.solver.LPSolver
import org.gnu.glpk.GLPK
import org.gnu.glpk.GLPKConstants
import org.gnu.glpk.SWIGTYPE_p_double
import org.gnu.glpk.SWIGTYPE_p_int
import org.gnu.glpk.glp_iocp
import org.gnu.glpk.glp_prob
import kotlin.system.measureTimeMillis

open class GlpkLpSolver(model: LPModel) : LPSolver<glp_prob>(model) {

  companion object {
    val solutionStatesWithoutResults =
        setOf(LPSolutionStatus.UNBOUNDED, LPSolutionStatus.INFEASIBLE, LPSolutionStatus.UNKNOWN)
  }

  private var glpkModel: glp_prob? = null

  private var variableMap: MutableMap<String, Int> = mutableMapOf()

  private var constraintMap: MutableMap<String, Int> = mutableMapOf()

  private val intOptConfig: () -> glp_iocp = {
    glp_iocp().apply {
      GLPK.glp_init_iocp(this)
      this.presolve = GLPKConstants.GLP_ON
    }
  }

  override fun initModel(): Boolean {
    return try {
      glpkModel = GLPK.glp_create_prob()
      GLPK.glp_set_prob_name(glpkModel, model.identifier)
      true
    } catch (e: Exception) {
      log.error { "Error while initializing GLPK Problem instance $e" }
      false
    }
  }

  override fun getBaseModel(): glp_prob? {
    return glpkModel
  }

  override fun solve(): LPSolutionStatus {
    try {
      log.info { "Starting computation of model" }
      val executionTime = measureTimeMillis {
        val iocp = intOptConfig()
        GLPK.glp_write_lp(glpkModel, null, "model.lp")
        GLPK.glp_intopt(glpkModel, iocp)
      }

      val solnStatus: LPSolutionStatus = getSolutionStatus(GLPK.glp_mip_status(glpkModel))
      log.info { "Computation terminated. Solution Status : $solnStatus, mip objective: " +
          "${GLPK.glp_mip_obj_val(glpkModel)} mip status: ${GLPK.glp_mip_status(glpkModel)}" }

      if (!solutionStatesWithoutResults.contains(solnStatus)) {
        val result: Double = GLPK.glp_get_obj_val(glpkModel)
        log.info { "Objective : $result" }
        model.solution = LPModelResult(solnStatus, result, executionTime, null)
        // Extract results and set it to variables
        log.info { "Extracting computed results to the model variables" }
        variableMap.entries.forEach { entry ->
          val varResult = GLPK.glp_mip_col_val(glpkModel, entry.value)
          model.variables.get(entry.key)?.populateResult(varResult)
          log.debug { "Variable ${entry.key} has value ${model.variables.get(entry.key)?.result}" }
        }
      } else {
        log.info { "Solution status : $solnStatus" }
        model.solution = LPModelResult(solnStatus)
      }
      return solnStatus
    } catch (e: Exception) {
      log.error { "Exception while computing the GLPK model : $e" }
      model.solution = LPModelResult(LPSolutionStatus.ERROR)
      return LPSolutionStatus.ERROR
    }
  }

  /** Function to initialize all variables in the GLPK Model. Returns false in case any variable initialization fails
   */
  override fun initVars(): Boolean {
    log.info { "Initializing variables" }
    model.variables.allValues().forEach { lpVar ->
      try {
        log.debug { "Initializing variable ($lpVar)" }
        val index: Int = GLPK.glp_add_cols(glpkModel, 1)
        GLPK.glp_set_col_name(glpkModel, index, lpVar.identifier)
        GLPK.glp_set_col_kind(glpkModel, index, getGlpVarType(lpVar.type))
        val boundType: Int = if (lpVar.lbound == lpVar.ubound) GLPKConstants.GLP_FX else GLPKConstants.GLP_DB
        GLPK.glp_set_col_bnds(glpkModel, index, boundType, lpVar.lbound, lpVar.ubound)
        variableMap[lpVar.identifier] = index
      } catch (e: Exception) {
        log.error { "Error while initializing GLPK Var ($lpVar) : $e" }
        return false
      }
    }
    return true
  }

  /** Function to initialize constraints in the GLPK Model. Returns false in case any constraint initialization fails */
  override fun initConstraints(): Boolean {
    log.info { "Initializing constraints" }
    model.constraints.allValues().forEach { lpConstraint ->
      try {
        log.debug { "Initializing Constraint ($lpConstraint)" }
        val reducedConstraint: LPConstraint? = model.reduce(lpConstraint)
        if (reducedConstraint == null) {
          log.error { "Reduced constraint could not be computed for constraint ${lpConstraint.identifier}" }
          return false
        }
        // Initialize constraint row in the model
        val index = GLPK.glp_add_rows(glpkModel, 1)
        GLPK.glp_set_row_name(glpkModel, index, lpConstraint.identifier)

        // Get the constant contribution from the RHS
        val constant: Double? = reducedConstraint.rhs.expression
            .map { term -> if (term.coefficient != null) term.coefficient else 0.0 }
            .reduce { u, v -> u!! + v!! }

        if (constant == null) {
          log.error { "Constant contribution not found in the reduced expression for constraint " +
              lpConstraint.identifier
          }
          return false
        }

        // set bounds
        when (reducedConstraint.operator) {
          LPOperator.LESS_EQUAL -> GLPK.glp_set_row_bnds(glpkModel, index, GLPKConstants.GLP_UP, 0.0, constant)
          LPOperator.GREATER_EQUAL -> GLPK.glp_set_row_bnds(glpkModel, index, GLPKConstants.GLP_LO, constant, 0.0)
          LPOperator.EQUAL -> GLPK.glp_set_row_bnds(glpkModel, index, GLPKConstants.GLP_FX, constant, constant)
          else -> {
            log.error { "Bound handling not supported in GLPK for operator ${reducedConstraint.operator} in " +
                "constraint ${lpConstraint.identifier}" }
            return false
          }
        }

        // initialize variables and coefficients
        val ind: SWIGTYPE_p_int = GLPK.new_intArray(reducedConstraint.lhs.expression.size)
        val coefficients: SWIGTYPE_p_double = GLPK.new_doubleArray(reducedConstraint.lhs.expression.size)
        var colIndex = 1
        for (term in reducedConstraint.lhs.expression) {
          GLPK.intArray_setitem(ind, colIndex, variableMap[term.lpVarIdentifier]!!)
          GLPK.doubleArray_setitem(coefficients, colIndex, term.coefficient!!)
          colIndex++
        }
        GLPK.glp_set_mat_row(glpkModel, index, reducedConstraint.lhs.expression.size, ind, coefficients)

        // Cleanup temporary arrays initialized in GLPK
        GLPK.delete_intArray(ind)
        GLPK.delete_doubleArray(coefficients)

        // define row in the model
        constraintMap[lpConstraint.identifier] = index
      } catch (e: Exception) {
        log.error { "Error while initializing GLPK Constraint ($lpConstraint) : $e" }
        return false
      }
    }
    return true
  }

  override fun initObjectiveFunction(): Boolean {
    return try {
      GLPK.glp_set_obj_name(glpkModel, "Objective Function")
      when (model.objective.objective) {
        LPObjectiveType.MINIMIZE -> GLPK.glp_set_obj_dir(glpkModel, GLPKConstants.GLP_MIN)
        LPObjectiveType.MAXIMIZE -> GLPK.glp_set_obj_dir(glpkModel, GLPKConstants.GLP_MAX)
        else -> {
          log.error { "Support not included to handle objective type ${model.objective.objective} in GLPK" }
          return false
        }
      }

      val reducedObjective = model.reduce(model.objective)
      if (reducedObjective==null) {
        log.error { "Could not compute reduced objective function" }
        return false
      }
      reducedObjective.expression.expression.forEach{
        if (it.isConstant()) {
          GLPK.glp_set_obj_coef(glpkModel, 0, it.coefficient!!)
        } else {
          GLPK.glp_set_obj_coef(glpkModel, variableMap[it.lpVarIdentifier]!!, it.coefficient!!)
        }
      }
      true
    } catch (e: Exception) {
      log.error { "Error while initializing Objective function : $e" }
      false
    }
  }

  internal fun getGlpVarType(type: LPVarType): Int {
    return when (type) {
      LPVarType.BOOLEAN -> GLPKConstants.GLP_BV
      LPVarType.INTEGER -> GLPKConstants.GLP_IV
      LPVarType.DOUBLE -> GLPKConstants.GLP_CV
      else -> GLPKConstants.GLP_CV
    }
  }

  internal fun getSolutionStatus(solutionStatus: Int): LPSolutionStatus {
    return when (solutionStatus) {
      GLPKConstants.GLP_UNDEF -> LPSolutionStatus.UNKNOWN
      GLPKConstants.GLP_OPT -> LPSolutionStatus.OPTIMAL
      GLPKConstants.GLP_FEAS -> LPSolutionStatus.TIME_LIMIT
      GLPKConstants.GLP_INFEAS -> LPSolutionStatus.INFEASIBLE
      GLPKConstants.GLP_UNBND -> LPSolutionStatus.UNBOUNDED
      else -> { LPSolutionStatus.UNKNOWN }
    }
  }
}