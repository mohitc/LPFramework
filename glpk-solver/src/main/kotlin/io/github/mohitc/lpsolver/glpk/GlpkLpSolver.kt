package io.github.mohitc.lpsolver.glpk

import com.lpapi.ffm.glpk.GLPKBoundType
import com.lpapi.ffm.glpk.GLPKFeatureStatus
import com.lpapi.ffm.glpk.GLPKMessageLevel
import com.lpapi.ffm.glpk.GLPKMipStatus
import com.lpapi.ffm.glpk.GLPKObjective
import com.lpapi.ffm.glpk.GLPKProblem
import com.lpapi.ffm.glpk.GLPKVarKind
import com.lpapi.ffm.glpk.GlpIocp
import io.github.mohitc.lpapi.model.LPConstraint
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPModelResult
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import io.github.mohitc.lpsolver.LPSolver
import kotlin.system.measureTimeMillis

open class GlpkLpSolver(
  model: LPModel,
) : LPSolver<GLPKProblem>(model) {
  companion object {
    val solutionStatesWithoutResults =
      setOf(LPSolutionStatus.UNBOUNDED, LPSolutionStatus.INFEASIBLE, LPSolutionStatus.UNKNOWN)
  }

  private var glpkModel: GLPKProblem = GLPKProblem()

  private var variableMap: MutableMap<String, Int> = mutableMapOf()

  private var constraintMap: MutableMap<String, Int> = mutableMapOf()

  private var cfg: GlpIocp =
    GlpIocp(
      preSolve = GLPKFeatureStatus.ON,
      messageLevel = GLPKMessageLevel.MSG_ON,
      binarize = GLPKFeatureStatus.ON,
    )

  override fun initModel(): Boolean =
    try {
      log.info { "Creating new GLPK problem instance with name ${model.identifier}" }
      glpkModel.setModelName(model.identifier)
      true
    } catch (e: Exception) {
      log.error { "Error while initializing GLPK Problem instance $e" }
      false
    }

  override fun getBaseModel(): GLPKProblem = glpkModel

  override fun solve(): LPSolutionStatus {
    try {
      log.info { "Starting computation of model" }
      val executionTime =
        measureTimeMillis {
          glpkModel.intopt(cfg)
        }

      val solnStatus: LPSolutionStatus = getSolutionStatus(glpkModel.mipStatus())
      log.info {
        "Computation terminated. Solution Status : $solnStatus, mip objective: " +
          "${glpkModel.mipObjectiveValue()} mip status: ${glpkModel.mipStatus()}"
      }

      if (!solutionStatesWithoutResults.contains(solnStatus)) {
        val result: Double = glpkModel.mipObjectiveValue()
        log.info { "Objective : $result" }
        model.solution = LPModelResult(solnStatus, result, executionTime, null)
        // Extract results and set it to variables
        log.info { "Extracting computed results to the model variables" }
        variableMap.entries.forEach { entry ->
          val varResult = glpkModel.mipColVal(entry.value)
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
        log.error { "Initializing variable ($lpVar)" }
        val index: Int = glpkModel.addCols(1)
        glpkModel.setColName(index, lpVar.identifier)
        glpkModel.setColKind(index, getGlpVarType(lpVar.type))
        val boundType: GLPKBoundType =
          if (lpVar.lbound ==
            lpVar.ubound
          ) {
            GLPKBoundType.FIXED
          } else {
            GLPKBoundType.DOUBLE_BOUNDED
          }
        glpkModel.setColBounds(index, boundType, lpVar.lbound, lpVar.ubound)
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
        log.error { "Initializing Constraint ($lpConstraint)" }
        val reducedConstraint: LPConstraint? = model.reduce(lpConstraint)
        if (reducedConstraint == null) {
          log.error { "Reduced constraint could not be computed for constraint ${lpConstraint.identifier}" }
          return false
        }
        // Initialize constraint row in the model
        val index = glpkModel.addRows(1)
        log.error { "Initialized row (Index: $index)" }
        glpkModel.setRowName(index, lpConstraint.identifier)
        log.error { "Set row name (Index: $index, ${lpConstraint.identifier})" }

        // Get the constant contribution from the RHS
        val constant: Double? =
          reducedConstraint.rhs.expression
            .map { term -> if (term.coefficient != null) term.coefficient else 0.0 }
            .reduce { u, v -> u!! + v!! }

        if (constant == null) {
          log.error {
            "Constant contribution not found in the reduced expression for constraint " +
              lpConstraint.identifier
          }
          return false
        }

        // set bounds
        when (reducedConstraint.operator) {
          LPOperator.LESS_EQUAL -> glpkModel.setRowBounds(index, GLPKBoundType.UPPER_BOUNDED, 0.0, constant)
          LPOperator.GREATER_EQUAL -> glpkModel.setRowBounds(index, GLPKBoundType.LOWER_BOUNDED, constant, 0.0)
          LPOperator.EQUAL -> glpkModel.setRowBounds(index, GLPKBoundType.FIXED, constant, constant)
          else -> {
            log.error {
              "Bound handling not supported in GLPK for operator ${reducedConstraint.operator} in " +
                "constraint ${lpConstraint.identifier}"
            }
            return false
          }
        }
        // initialize variables and coefficients
        val ind = mutableListOf<Int>()
        val coefficients = mutableListOf<Double>()
        for (term in reducedConstraint.lhs.expression) {
          ind.addLast(variableMap[term.lpVarIdentifier]!!)
          coefficients.addLast(term.coefficient!!)
        }

        glpkModel.setMatrixRow(index, reducedConstraint.lhs.expression.size, ind, coefficients)

        // define row in the model
        constraintMap[lpConstraint.identifier] = index
      } catch (e: Exception) {
        log.error { "Error while initializing GLPK Constraint ($lpConstraint) : ${e.stackTraceToString()}" }
        return false
      }
    }
    return true
  }

  override fun initObjectiveFunction(): Boolean {
    return try {
      log.error { "Initializing objective function" }

      glpkModel.setObjectiveName("Objective Function")
      when (model.objective.objective) {
        LPObjectiveType.MINIMIZE -> glpkModel.setObjective(GLPKObjective.MINIMIZE)
        LPObjectiveType.MAXIMIZE -> glpkModel.setObjective(GLPKObjective.MAXIMIZE)
        else -> {
          log.error { "Support not included to handle objective type ${model.objective.objective} in GLPK" }
          return false
        }
      }

      val reducedObjective = model.reduce(model.objective)
      if (reducedObjective == null) {
        log.error { "Could not compute reduced objective function" }
        return false
      }
      reducedObjective.expression.expression.forEach {
        if (it.isConstant()) {
          glpkModel.setObjectiveCoefficient(0, it.coefficient!!)
        } else {
          glpkModel.setObjectiveCoefficient(variableMap[it.lpVarIdentifier]!!, it.coefficient!!)
        }
      }
      log.error { "Objective function Initialized" }
      true
    } catch (e: Exception) {
      log.error { "Error while initializing Objective function : $e" }
      false
    }
  }

  internal fun getGlpVarType(type: LPVarType): GLPKVarKind =
    when (type) {
      LPVarType.BOOLEAN -> GLPKVarKind.BOOLEAN
      LPVarType.INTEGER -> GLPKVarKind.INTEGER
      LPVarType.DOUBLE -> GLPKVarKind.CONTINUOUS
      else -> GLPKVarKind.CONTINUOUS
    }

  internal fun getSolutionStatus(solutionStatus: GLPKMipStatus?): LPSolutionStatus =
    when (solutionStatus) {
      GLPKMipStatus.UNDEFINED -> LPSolutionStatus.UNKNOWN
      GLPKMipStatus.OPTIMAL -> LPSolutionStatus.OPTIMAL
      GLPKMipStatus.FEASIBLE -> LPSolutionStatus.TIME_LIMIT
      GLPKMipStatus.NOFEASIBLE -> LPSolutionStatus.INFEASIBLE
      else -> {
        LPSolutionStatus.UNKNOWN
      }
    }
}