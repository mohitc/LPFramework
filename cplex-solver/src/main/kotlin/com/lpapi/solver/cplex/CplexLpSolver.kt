package com.lpapi.solver.cplex

import com.lpapi.model.LPConstraint
import com.lpapi.model.LPExpression
import com.lpapi.model.LPModel
import com.lpapi.model.LPModelResult
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType
import com.lpapi.solver.LPSolver
import ilog.concert.IloConstraint
import ilog.concert.IloLinearNumExpr
import ilog.concert.IloNumVar
import ilog.concert.IloNumVarType
import ilog.cplex.IloCplex
import kotlin.system.measureTimeMillis

class CplexLpSolver(
  model: LPModel,
) : LPSolver<IloCplex>(model) {
  private var cplexModel: IloCplex? = null

  private var variableMap: MutableMap<String, IloNumVar> = mutableMapOf()

  private var constraintMap: MutableMap<String, IloConstraint> = mutableMapOf()

  private var cplexObjective: IloLinearNumExpr? = null

  override fun initModel(): Boolean {
    try {
      log.info { "Initializing cplex model for ${model.identifier}" }
      cplexModel = IloCplex()
    } catch (e: Exception) {
      log.error { "Error while initializing CPLEX Model $e" }
      return false
    }
    return true
  }

  override fun getBaseModel(): IloCplex? = cplexModel

  override fun solve(): LPSolutionStatus {
    try {
      log.info { "Starting computation of model" }
      val executionTime =
        measureTimeMillis {
          cplexModel?.solve()
        }
      val solutionStatus = getSolutionStatus(cplexModel?.status)
      log.info { "Computation terminated. Solution Status : $solutionStatus" }
      if (solutionStatus == LPSolutionStatus.ERROR) {
        log.error { "Could not determine solution status" }
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
      } else if (!(
          solutionStatus == LPSolutionStatus.INFEASIBLE ||
            solutionStatus == LPSolutionStatus.UNBOUNDED ||
            solutionStatus == LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED
        )
      ) {
        // add model results
        extractResults()
        model.solution =
          LPModelResult(
            solutionStatus,
            cplexModel?.getValue(cplexObjective),
            executionTime,
            cplexModel?.mipRelativeGap,
          )
        log.info { "${model.solution}" }
      } else {
        model.solution = LPModelResult(solutionStatus)
      }
      return solutionStatus
    } catch (e: Exception) {
      log.error("Error while computing CPLEX model", e)
      model.solution = LPModelResult(LPSolutionStatus.ERROR)
      return LPSolutionStatus.ERROR
    }
  }

  /** Function to extract the results of the LP model into the corresponding variables in the model
   */
  @Throws(Exception::class)
  private fun extractResults() {
    log.info { "Extracting results of computed model into the variables" }
    variableMap.entries.forEach { entry ->
      try {
        cplexModel?.getValue(entry.value)?.let { model.variables.get(entry.key)?.populateResult(it) }
      } catch (e: Exception) {
        log.error { "Error while extracting results from CPLEX model for variable ${entry.key} : $e" }
        throw e
      }
    }
  }

  fun getSolutionStatus(cplexStatus: IloCplex.Status?): LPSolutionStatus =
    when (cplexStatus) {
      IloCplex.Status.Optimal -> LPSolutionStatus.OPTIMAL
      IloCplex.Status.InfeasibleOrUnbounded -> LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED
      IloCplex.Status.Infeasible -> LPSolutionStatus.INFEASIBLE
      IloCplex.Status.Unbounded -> LPSolutionStatus.UNBOUNDED
      IloCplex.Status.Bounded -> LPSolutionStatus.BOUNDED
      IloCplex.Status.Error -> LPSolutionStatus.ERROR
      null -> LPSolutionStatus.ERROR
      else -> {
        LPSolutionStatus.UNKNOWN
      }
    }

  /** Function to initialize all variables in the model
   *
   */
  override fun initVars(): Boolean {
    log.info { "Initializing variables" }
    model.variables.allValues().forEach { lpVar ->
      try {
        log.debug { "Initializing variable ($lpVar)" }
        val cplexVar = cplexModel?.numVar(lpVar.lbound, lpVar.ubound, getCplexVarType(lpVar.type), lpVar.identifier)
        if (cplexVar != null) {
          variableMap[lpVar.identifier] = cplexVar
        } else {
          log.error {
            "Variable not initialized when attempting to create cplexModel?.numVar(" +
              "${lpVar.lbound}, ${lpVar.ubound}, ${getCplexVarType(lpVar.type)}, ${lpVar.identifier})"
          }
          return false
        }
      } catch (e: Exception) {
        log.error { "Error while initializing Cplex Var ($lpVar) : $e" }
        return false
      }
    }
    return true
  }

  override fun initObjectiveFunction(): Boolean {
    log.info { "Initializing Objective Function" }
    try {
      // Initialize cplex objective, to be used later to extract value of the objective function
      val reducedExpression = model.reduce(model.objective.expression)
      if (reducedExpression == null) {
        log.error { "Objective function ${model.objective} could not be reduced" }
        return false
      }
      cplexObjective = generateExpression(reducedExpression)
      if (cplexObjective == null) {
        log.error { "Error while generating objective function" }
        return false
      }
      when (model.objective.objective) {
        LPObjectiveType.MAXIMIZE -> cplexModel?.addMaximize(cplexObjective)
        LPObjectiveType.MINIMIZE -> cplexModel?.addMinimize(cplexObjective)
        else -> {
          log.error { "Mechanism not implemented to support objective type ${model.objective.objective}" }
          null
        }
        // If objective if not created, return false otherwise return true
      } ?: return false
      return true
    } catch (e: Exception) {
      log.error { "Exception while generating CPLEX Objective function $e" }
      return false
    }
  }

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
        val lhs = generateExpression(lpConstraint.lhs)
        val rhs = generateExpression(lpConstraint.rhs)
        if (lhs == null || rhs == null) {
          log.error { "Error while generating expression for model constraint ${lpConstraint.identifier}" }
          return false
        }
        val modelConstraint: IloConstraint? =
          when (lpConstraint.operator) {
            LPOperator.LESS_EQUAL -> {
              cplexModel?.addLe(lhs, rhs, lpConstraint.identifier)
            }
            LPOperator.GREATER_EQUAL -> {
              cplexModel?.addGe(lhs, rhs, lpConstraint.identifier)
            }
            LPOperator.EQUAL -> {
              cplexModel?.addEq(lhs, rhs, lpConstraint.identifier)
            }
            else -> {
              null
            }
          }
        if (modelConstraint == null) {
          log.error { "Error while generating model constraint ${lpConstraint.identifier}" }
          return false
        }
        constraintMap[lpConstraint.identifier] = modelConstraint
      } catch (e: Exception) {
        log.error { "Error while initializing CPLEX Constraint ($lpConstraint) : $e" }
        return false
      }
    }
    return true
  }

  /** Function to generate CPLEX linear expressions based on LPModel expressions which are used in generating the
   * Objective function as well as the model constraints.
   */
  private fun generateExpression(expr: LPExpression): IloLinearNumExpr? {
    try {
      val cplexExpr: IloLinearNumExpr? = cplexModel?.linearNumExpr()
      if (cplexExpr == null) {
        log.info { "Unexpected error while generating expression. Check is model is initialized" }
        return null
      }
      val reducedExpr = model.reduce(expr)
      if (reducedExpr == null) {
        log.error { "Error in reducing expression. Please check the logs" }
        return null
      }
      reducedExpr.expression.forEach { expTerm ->
        if (expTerm.isConstant()) {
          cplexExpr.constant = expTerm.coefficient!!
        } else {
          cplexExpr.addTerm(expTerm.coefficient!!, variableMap[expTerm.lpVarIdentifier])
        }
      }
      return cplexExpr
    } catch (e: Exception) {
      log.error { "Error while generating expression $e" }
      return null
    }
  }

  internal fun getCplexVarType(type: LPVarType): IloNumVarType? =
    when (type) {
      LPVarType.BOOLEAN -> IloNumVarType.Bool
      LPVarType.INTEGER -> IloNumVarType.Int
      LPVarType.DOUBLE -> IloNumVarType.Float
      else -> {
        null
      }
    }
}