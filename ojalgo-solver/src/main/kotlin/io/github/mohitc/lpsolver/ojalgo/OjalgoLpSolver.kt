package io.github.mohitc.lpsolver.ojalgo

import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPModelResult
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import io.github.mohitc.lpsolver.LPSolver
import org.ojalgo.optimisation.Expression
import org.ojalgo.optimisation.ExpressionsBasedModel
import org.ojalgo.optimisation.Optimisation
import org.ojalgo.optimisation.Optimisation.State
import org.ojalgo.optimisation.Variable
import kotlin.system.measureTimeMillis

class OjalgoLpSolver(
  model: LPModel,
) : LPSolver<ExpressionsBasedModel>(model) {
  private var ojalgoModel: ExpressionsBasedModel? = ExpressionsBasedModel()

  private var variableMap: MutableMap<String, Variable> = mutableMapOf()

  private val constraintMap: MutableMap<String, Expression> = mutableMapOf()

  override fun initModel(): Boolean {
    // TODO("Not yet implemented")
    return true
  }

  override fun getBaseModel(): ExpressionsBasedModel? = ojalgoModel

  private fun getSolutionStatus(state: State): LPSolutionStatus =
    when (state) {
      State.APPROXIMATE -> LPSolutionStatus.UNKNOWN
      State.DISTINCT -> LPSolutionStatus.OPTIMAL
      State.FAILED -> LPSolutionStatus.ERROR
      State.FEASIBLE -> LPSolutionStatus.UNKNOWN
      State.INFEASIBLE -> LPSolutionStatus.INFEASIBLE
      State.INVALID -> LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED
      State.OPTIMAL -> LPSolutionStatus.OPTIMAL
      State.UNBOUNDED -> LPSolutionStatus.UNBOUNDED
      State.UNEXPLORED -> LPSolutionStatus.UNKNOWN
      State.VALID -> LPSolutionStatus.UNKNOWN
    }

  private fun extractResults(): Boolean {
    variableMap.entries.forEach { (k, v) ->
      try {
        model.variables.get(k)!!.populateResult(v.value.toDouble())
      } catch (e: Exception) {
        log.error { "Error while extracting value for $k: $e" }
        return false
      }
    }
    return true
  }

  override fun solve(): LPSolutionStatus {
    try {
      log.info { "Starting computation of model" }
      var modelResult: Optimisation.Result
      val executionTime =
        measureTimeMillis {
          modelResult =
            when (model.objective.objective) {
              LPObjectiveType.MAXIMIZE -> ojalgoModel!!.maximise()
              LPObjectiveType.MINIMIZE -> ojalgoModel!!.minimise()
            }
        }
      log.info { "Computation terminated. Solution Status : ${modelResult.state}" }
      val solutionStatus = getSolutionStatus(modelResult.state)
      if (solutionStatus == LPSolutionStatus.INFEASIBLE ||
        solutionStatus == LPSolutionStatus.UNBOUNDED ||
        solutionStatus == LPSolutionStatus.ERROR
      ) {
        model.solution = LPModelResult(solutionStatus)
        return solutionStatus
      }
      val resultsOkay = extractResults()
      if (!resultsOkay) {
        log.error { "extractResults() want true got false" }
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
        return solutionStatus
      }
      val objectiveVal = model.evaluate(model.objective.expression)
      if (objectiveVal == null) {
        log.error { "evaluate(${model.objective.expression}) want non-null got null" }
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
        return solutionStatus
      }
      model.solution =
        LPModelResult(
          solutionStatus,
          objectiveVal,
          executionTime,
          if (solutionStatus == LPSolutionStatus.OPTIMAL) {
            0.0
          } else {
            null
          },
        )
      return solutionStatus
    } catch (e: Exception) {
      log.error { "Error while solving the model: $e" }
      model.solution = LPModelResult(LPSolutionStatus.ERROR)
      return LPSolutionStatus.ERROR
    }
  }

  override fun initVars(): Boolean {
    log.info { "Initializing Variables" }
    model.variables.allValues().forEach { lpVar ->
      try {
        log.debug { "Initializing variable ($lpVar)" }
        val ojalgoVar =
          ojalgoModel!!.addVariable(lpVar.identifier).lower(lpVar.lbound).upper(lpVar.ubound).apply {
            when (lpVar.type) {
              LPVarType.INTEGER, LPVarType.BOOLEAN -> integer(true)
              LPVarType.DOUBLE -> integer(false)
            }
          }
        if (ojalgoVar == null) {
          log.info { "Could not create variable $lpVar" }
          return false
        }
        variableMap[lpVar.identifier] = ojalgoVar
      } catch (e: Exception) {
        log.error { "Error while initializing Ojalgo Val ($lpVar): $e" }
        return false
      }
    }
    return true
  }

  override fun initConstraints(): Boolean {
    log.info { "Initializing constraints" }
    model.constraints.allValues().forEach { lpConstraint ->
      try {
        val reducedExpression = model.reduce(lpConstraint)
        if (reducedExpression == null) {
          log.error { "reduced expression is empty. Skipping" }
        } else {
          val constTerm =
            reducedExpression.rhs.expression
              .filter { t -> t.isConstant() }
              .sumOf { t -> t.coefficient!! }
          val expr =
            ojalgoModel!!
              .addExpression(lpConstraint.identifier)
              .apply {
                when (reducedExpression.operator) {
                  LPOperator.LESS_EQUAL -> upper(constTerm)
                  LPOperator.EQUAL -> upper(constTerm).lower(constTerm)
                  LPOperator.GREATER_EQUAL -> lower(constTerm)
                }
              }.apply {
                reducedExpression.lhs.expression.filter { t -> !t.isConstant() }.forEach { t ->
                  set(variableMap[t.lpVarIdentifier], t.coefficient)
                }
              }
          if (expr == null) {
            log.error { "Unexpected (null) value while creating constraint ($lpConstraint)" }
            return false
          }
          constraintMap[lpConstraint.identifier] = expr
        }
      } catch (e: Exception) {
        log.error { "Error while initializing constraint ($lpConstraint): $e" }
        return false
      }
    }
    return true
  }

  override fun initObjectiveFunction(): Boolean {
    log.info { "Initializing Objective Function" }
    return try {
      val reducedObjective =
        model
          .reduce(model.objective.expression)
      reducedObjective?.expression?.forEach { t ->
        if (t.isConstant()) {
          log.debug { "Constant terms are not supported in ojalgo. Including them in objective value calculation only" }
        } else {
          variableMap[t.lpVarIdentifier]!!.weight(t.coefficient!!)
        }
      }
      true
    } catch (e: Exception) {
      log.error { "Exception while configuring the optimization objective: $e" }
      return false
    }
  }
}