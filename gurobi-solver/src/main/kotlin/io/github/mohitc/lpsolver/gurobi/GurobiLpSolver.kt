package io.github.mohitc.lpsolver.gurobi

import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRBConstr
import com.gurobi.gurobi.GRBEnv
import com.gurobi.gurobi.GRBException
import com.gurobi.gurobi.GRBLinExpr
import com.gurobi.gurobi.GRBModel
import com.gurobi.gurobi.GRBVar
import io.github.mohitc.lpapi.model.LPConstraint
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPModelResult
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import io.github.mohitc.lpsolver.LPSolver
import kotlin.system.measureTimeMillis

class GurobiLpSolver(
  model: LPModel,
) : LPSolver<GRBModel>(model) {
  companion object {
    val solutionStatesWithoutResults =
      setOf(
        LPSolutionStatus.UNBOUNDED,
        LPSolutionStatus.INFEASIBLE,
        LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED,
        LPSolutionStatus.UNKNOWN,
      )
  }

  private var grbModel: GRBModel? = null

  private var variableMap: MutableMap<String, GRBVar> = mutableMapOf()

  private var constraintMap: MutableMap<String, GRBConstr> = mutableMapOf()

  override fun initModel(): Boolean {
    try {
      val env = GRBEnv()
      this.grbModel = GRBModel(env)
      return true
    } catch (e: GRBException) {
      log.error("Error in generating Gurobi model", e)
    }
    return false
  }

  override fun getBaseModel(): GRBModel? = grbModel

  override fun solve(): LPSolutionStatus {
    try {
      log.info { "Starting computation of model" }
      val executionTime =
        measureTimeMillis {
          grbModel?.optimize()
        }
      val solutionStatus =
        getSolutionStatus(
          grbModel?.get(GRB.IntAttr.Status),
        )
      log.info { "Computation terminated. Solution Status : $solutionStatus" }
      if (solutionStatus == LPSolutionStatus.ERROR) {
        log.error { "Could not determine solution status" }
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
      } else if (!(solutionStatesWithoutResults.contains(solutionStatus))) {
        // add model results
        extractResults()
        model.solution =
          LPModelResult(
            solutionStatus,
            grbModel?.get(GRB.DoubleAttr.ObjVal),
            executionTime,
            grbModel?.get(GRB.DoubleAttr.MIPGap),
          )
        log.info { "${model.solution}" }
      } else {
        model.solution = LPModelResult(solutionStatus)
      }
      return solutionStatus
    } catch (e: Exception) {
      log.error { "Error while computing Gurobi model: $e" }
      model.solution = LPModelResult(LPSolutionStatus.ERROR)
      return LPSolutionStatus.ERROR
    }
  }

  /** Function to extract the results of the LP model into the corresponding variables in the model
   */
  private fun extractResults() {
    log.info { "Extracting results of computed model into the variables" }
    for (entry in variableMap.entries) {
      try {
        model.variables.get(entry.key)?.populateResult(entry.value.get(GRB.DoubleAttr.X))
      } catch (e: Exception) {
        log.error { "Error while extracting results from Gurobi model : $e" }
        // clear flag to indicate that results were set for variables
        model.variables.allValues().forEach { it.resultSet = false }
        throw e
      }
    }
  }

  /**Function to get the LP Solution Status based on the Gurobi Model Status
   */
  internal fun getSolutionStatus(grbStatus: Int?): LPSolutionStatus =
    when (grbStatus) {
      GRB.Status.OPTIMAL -> LPSolutionStatus.OPTIMAL
      GRB.Status.UNBOUNDED -> LPSolutionStatus.UNBOUNDED
      GRB.Status.INFEASIBLE -> LPSolutionStatus.INFEASIBLE
      GRB.Status.INF_OR_UNBD -> LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED
      GRB.Status.TIME_LIMIT -> LPSolutionStatus.TIME_LIMIT
      GRB.Status.CUTOFF -> LPSolutionStatus.CUTOFF
      null -> LPSolutionStatus.ERROR
      else -> {
        LPSolutionStatus.UNKNOWN
      }
    }

  override fun initVars(): Boolean {
    log.info { "Initializing variables" }
    model.variables.allValues().forEach { lpVar ->
      try {
        log.debug { "Initializing variable ($lpVar)" }
        val grbVarType = getGurobiVarType(lpVar.type)
        if (grbVarType == null) {
          log.error { "Could not determine variable type for ${lpVar.type}" }
          return false
        }
        val grbVar = grbModel?.addVar(lpVar.lbound, lpVar.ubound, 0.0, grbVarType, lpVar.identifier)
        if (grbVar != null) {
          variableMap[lpVar.identifier] = grbVar
        } else {
          log.info { "Could not create variable for variable $lpVar" }
          return false
        }
      } catch (e: Exception) {
        log.error { "Error while initializing Gurobi Var ($lpVar) : $e" }
        return false
      }
    }
    return true
  }

  /** Function to get the Gurobi variable type from the LP Variable type
   */
  internal fun getGurobiVarType(type: LPVarType): Char? {
    return when (type) {
      LPVarType.INTEGER -> return GRB.INTEGER
      LPVarType.BOOLEAN -> return GRB.BINARY
      LPVarType.DOUBLE -> return GRB.CONTINUOUS
      else -> {
        null
      }
    }
  }

  /** Function to get the Gurobi operator from the LP Operator type
   */
  internal fun getGurobiOperator(operator: LPOperator): Char? =
    when (operator) {
      LPOperator.GREATER_EQUAL -> GRB.GREATER_EQUAL
      LPOperator.EQUAL -> GRB.EQUAL
      LPOperator.LESS_EQUAL -> GRB.LESS_EQUAL
      else -> {
        null
      }
    }

  override fun initConstraints(): Boolean {
    log.info { "Initializing constraints" }
    model.constraints.allValues().forEach { lpConstraint ->
      try {
        log.info { "Initializing Constraint ($lpConstraint)" }
        val reducedConstraint: LPConstraint? = model.reduce(lpConstraint)
        if (reducedConstraint == null) {
          log.error { "Reduced constraint could not be computed for constraint ${lpConstraint.identifier}" }
          return false
        }

        val gurobiOperator = getGurobiOperator(lpConstraint.operator)
        if (gurobiOperator == null) {
          log.error { "Could not determine operator for LP Model Operator ${lpConstraint.operator}" }
          return false
        }
        val modelConstraint: GRBConstr? =
          grbModel?.addConstr(
            generateExpression(lpConstraint.lhs),
            gurobiOperator,
            generateExpression(lpConstraint.rhs),
            lpConstraint.identifier,
          )
        if (modelConstraint == null) {
          log.error { "Error while generating model constraint ${lpConstraint.identifier}" }
          return false
        }
        constraintMap[lpConstraint.identifier] = modelConstraint
      } catch (e: Exception) {
        log.error { "Error while initializing Gurobi Constraint ($lpConstraint) : $e" }
        return false
      }
    }
    return true
  }

  override fun initObjectiveFunction(): Boolean {
    log.info { "Initializing Objective Function" }
    return try {
      // Initialize cplex objective, to be used later to extract value of the objective function
      val gurobiObjective = generateExpression(model.objective.expression)
      val objectiveType = getGurobiObjectiveType(model.objective.objective)
      if (gurobiObjective == null) {
        return false
      }
      grbModel?.setObjective(gurobiObjective, objectiveType)
      true
    } catch (e: Exception) {
      log.error { "Exception while generating Gurobi Objective function $e" }
      false
    }
  }

  /** Function to get the Gurobi Objective type based on the LP ObjectiveType
   */
  internal fun getGurobiObjectiveType(objType: LPObjectiveType): Int =
    when (objType) {
      LPObjectiveType.MINIMIZE -> GRB.MINIMIZE
      LPObjectiveType.MAXIMIZE -> GRB.MAXIMIZE
    }

  /** Function to generate Gurobi linear expressions based on LPModel expressions which are used in generating the
   * Objective function as well as the model constraints.
   */
  private fun generateExpression(expr: io.github.mohitc.lpapi.model.LPExpression): GRBLinExpr? {
    try {
      val linExpr = GRBLinExpr()
      val reducedExpr = model.reduce(expr)
      if (reducedExpr == null) {
        log.error { "Error in reducing expression. Please check the logs" }
        return null
      }
      reducedExpr.expression.forEach { term ->
        term.coefficient?.let { const ->
          apply {
            if (term.isConstant()) {
              linExpr.addConstant(const)
            } else {
              linExpr.addTerm(const, variableMap[term.lpVarIdentifier])
            }
          }
        }
      }
      return linExpr
    } catch (e: Exception) {
      log.error { "Error while generating linear expression for Gurobi model: $e" }
      return null
    }
  }
}