package com.lpapi.solver.glpk

import com.lpapi.model.LPConstraint
import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPVarType
import com.lpapi.solver.LPSolver
import com.lpapi.solver.enums.LPSolutionStatus
import mu.KotlinLogging
import org.gnu.glpk.*

open class GlpkLpSolver(model: LPModel) : LPSolver<glp_prob>(model) {

  private var glpkModel : glp_prob? = null

  private var variableMap : MutableMap<String, Int> = mutableMapOf()

  private var constraintMap : MutableMap<String, Int> = mutableMapOf()

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
      val iocp = glp_iocp()
      GLPK.glp_init_iocp(iocp)
      iocp.presolve = GLPKConstants.GLP_ON
      GLPK.glp_write_lp(glpkModel, null, "model.lp")
      GLPK.glp_intopt(glpkModel, iocp)

      GLPK.glp_mip_obj_val(glpkModel)
      val solnStatus: LPSolutionStatus = getSolutionStatus(GLPK.glp_mip_status(glpkModel))
      log.info { "Computation terminated. Solution Status : $solnStatus " }

      if (solnStatus !== LPSolutionStatus.UNKNOWN && solnStatus !== LPSolutionStatus.INFEASIBLE) {
        val result : Double = GLPK.glp_get_obj_val(glpkModel)
        log.info {"Objective : $result"}
        model.objective.result = result
        //Extract results and set it to variables
        log.info { "Extracting computed results to the model variables" }
        variableMap.entries.forEach { entry ->
          val result = GLPK.glp_mip_col_val (glpkModel, entry.value)
          model.variables.get(entry.key)?.populateResult(result)
          log.debug { "Variable ${entry.key} has value ${model.variables.get(entry.key)?.result}" }
        }
      }
      return solnStatus
    } catch (e: Exception) {
      log.error { "Exception while computing the GLPK model : $e" }
      return LPSolutionStatus.UNKNOWN
    }
  }


  /** Function to initialize all variables in the GLPK Model. Returns false in case any variable initialization fails
   */
  override fun initVars() : Boolean {
    log.info { "Initializing variables" }
    model.variables.allValues().forEach { lpVar ->
      try {
        log.debug { "Initializing variable ($lpVar)" }
        val index: Int = GLPK.glp_add_cols(glpkModel, 1)
        variableMap[lpVar.identifier] = index
        GLPK.glp_set_col_name(glpkModel, index, lpVar.identifier)
        GLPK.glp_set_col_kind(glpkModel, index, getGlpVarType(lpVar.type))
        val boundType: Int = if (lpVar.lbound == lpVar.ubound) GLPKConstants.GLP_FX else GLPKConstants.GLP_BV
        GLPK.glp_set_col_bnds(glpkModel, index, boundType, lpVar.lbound, lpVar.ubound)
      } catch (e: Exception) {
        log.error { "Error while initializing GLPK Var ($lpVar) : $e" }
        return false
      }
    }
    return true
  }

  /** Function to initialize all constraints in the GLPK Model. Returns false in case any constraint initialization fails
   */
  override fun initConstraints() : Boolean {
    log.info { "Initializing constraints" }
    model.constraints.allValues().forEach { lpConstraint ->
      try {
        log.debug { "Initializing Constraint ($lpConstraint)" }
        val reducedConstraint : LPConstraint? = model.reduce(lpConstraint)
        if (reducedConstraint==null) {
          log.error { "Reduced constraint could not be computed for constriat ${lpConstraint.identifier}" }
          return false
        }
        //Initialize constraint row in the model
        val index = GLPK.glp_add_rows(glpkModel, 1)
        GLPK.glp_set_row_name(glpkModel, index, lpConstraint.identifier)
        constraintMap.put(lpConstraint.identifier, index)

        //Get the constant contribution from the RHS
        val constant : Double? = reducedConstraint.rhs.expression
            .map { term -> if (term.coefficient!=null) term.coefficient else 0.0 }
            .reduce{ u, v -> u!! + v!! }

        if (constant==null) {
          log.error { "Constant contribution not found in the reduced expression for constraint ${lpConstraint.identifier}" }
          return false
        }

        //set bounds
        when (reducedConstraint.operator) {
          LPOperator.LESS_EQUAL -> GLPK.glp_set_row_bnds(glpkModel, index, GLPKConstants.GLP_UP, 0.0, constant)
          LPOperator.GREATER_EQUAL -> GLPK.glp_set_row_bnds(glpkModel, index, GLPKConstants.GLP_LO, constant, 0.0)
          LPOperator.EQUAL -> GLPK.glp_set_row_bnds(glpkModel, index, GLPKConstants.GLP_FX, constant, constant)
          else -> {
            log.error { "Bound handling not supported in GLPK for operator ${reducedConstraint.operator} in constraint ${lpConstraint.identifier}" }
            return false
          }
        }

        //initialize variables and coefficients
        val ind : SWIGTYPE_p_int = GLPK.new_intArray(reducedConstraint.lhs.expression.size)
        val coefficients : SWIGTYPE_p_double = GLPK.new_doubleArray(reducedConstraint.lhs.expression.size)
        var colIndex = 1
        for (term in reducedConstraint.lhs.expression) {
          GLPK.intArray_setitem(ind, colIndex, variableMap[term.lpVarIdentifier]!!)
          GLPK.doubleArray_setitem(coefficients, colIndex, term.coefficient!!)
          colIndex++
        }
        GLPK.glp_set_mat_row(glpkModel, index, reducedConstraint.lhs.expression.size, ind, coefficients)


        //Cleanup temporary arrays initialized in GLPK
        GLPK.delete_intArray(ind)
        GLPK.delete_doubleArray(coefficients)
      } catch (e: Exception) {
        log.error { "Error while initializing GLPK Constraint ($lpConstraint) : $e" }
        return false
      }
    }
    return true
  }

  override fun initObjectiveFunction() : Boolean {
    try {
      GLPK.glp_set_obj_name(glpkModel, "Objective Function")
      when (model.objective.objective) {
        LPObjectiveType.MINIMIZE -> GLPK.glp_set_obj_dir(glpkModel, GLPKConstants.GLP_MIN)
        LPObjectiveType.MAXIMIZE -> GLPK.glp_set_obj_dir(glpkModel, GLPKConstants.GLP_MAX)
        else ->  {
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
    } catch (e: Exception) {
      log.error { "Error while initializing Objective function : $e" }
      return false
    }
    return true
  }

  internal fun getGlpVarType(type: LPVarType) : Int {
    return when (type) {
      LPVarType.BOOLEAN -> GLPKConstants.GLP_BV
      LPVarType.INTEGER -> GLPKConstants.GLP_IV
      LPVarType.DOUBLE -> GLPKConstants.GLP_CV
      else -> GLPKConstants.GLP_CV
    }
  }

  private fun getSolutionStatus(solutionStatus: Int) : LPSolutionStatus {
    return when (solutionStatus) {
      GLPKConstants.GLP_UNDEF -> LPSolutionStatus.UNKNOWN
      GLPKConstants.GLP_OPT -> LPSolutionStatus.OPTIMAL
      GLPKConstants.GLP_FEAS -> LPSolutionStatus.TIME_LIMIT
      GLPKConstants.GLP_INFEAS -> LPSolutionStatus.INFEASIBLE
      else -> { LPSolutionStatus.UNKNOWN }
    }
  }

}