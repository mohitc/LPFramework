package com.lpapi.solver.highs

import com.lpapi.ffm.highs.HIGHSInfoParam
import com.lpapi.ffm.highs.HIGHSModelStatus
import com.lpapi.ffm.highs.HIGHSObjective
import com.lpapi.ffm.highs.HIGHSProblem
import com.lpapi.ffm.highs.HIGHSStatus
import com.lpapi.ffm.highs.HIGHSVarType
import com.lpapi.model.LPConstraint
import com.lpapi.model.LPModel
import com.lpapi.model.LPModelResult
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType
import com.lpapi.solver.LPSolver
import kotlin.system.measureTimeMillis

class HighsLPSolver(
  model: LPModel,
) : LPSolver<HIGHSProblem>(model) {
  private var highsModel: HIGHSProblem = HIGHSProblem()

  private var variableMap: MutableMap<String, Int> = mutableMapOf()

  private var constraintMap: MutableMap<String, Int> = mutableMapOf()

  override fun initModel(): Boolean {
    // Add code to configure instance.
    return true
  }

  override fun getBaseModel() = highsModel

  private fun getSolutionStatus(modelStatus: HIGHSModelStatus): LPSolutionStatus =
    when (modelStatus) {
      HIGHSModelStatus.NOT_SET,
      HIGHSModelStatus.MODEL_EMPTY,
      HIGHSModelStatus.UNKNOWN,
      HIGHSModelStatus.INTERRUPT,
      -> LPSolutionStatus.UNKNOWN

      HIGHSModelStatus.LOAD_ERROR,
      HIGHSModelStatus.MODEL_ERROR,
      HIGHSModelStatus.PRESOLVE_ERROR,
      HIGHSModelStatus.POSTSOLVE_ERROR,
      HIGHSModelStatus.SOLVE_ERROR,
      -> LPSolutionStatus.ERROR

      HIGHSModelStatus.OPTIMAL -> LPSolutionStatus.OPTIMAL
      HIGHSModelStatus.INFEASIBLE -> LPSolutionStatus.INFEASIBLE
      HIGHSModelStatus.UNBOUNDED_OR_INFEASIBLE -> LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED
      HIGHSModelStatus.UNBOUNDED -> LPSolutionStatus.UNBOUNDED
      HIGHSModelStatus.TIME_LIMIT -> LPSolutionStatus.TIME_LIMIT
      HIGHSModelStatus.OBJECTIVE_BOUND,
      HIGHSModelStatus.OBJECTIVE_TARGET,
      HIGHSModelStatus.ITERATION_LIMIT,
      HIGHSModelStatus.SOLUTION_LIMIT,
      -> LPSolutionStatus.CUTOFF
    }

  override fun solve(): LPSolutionStatus {
    try {
      var retCode: HIGHSStatus
      val executionTime =
        measureTimeMillis {
          retCode = highsModel.run()
          log.info { "model.solve() return code $retCode" }

          if (retCode != HIGHSStatus.OK) {
            log.error { " highs.run() want return code OK got $retCode" }
            model.solution = LPModelResult(LPSolutionStatus.ERROR)
            return LPSolutionStatus.ERROR
          }
        }

      val modelStatus = highsModel.getModelStatus()
      log.info { "Native model status $modelStatus" }

      val solutionStatus = getSolutionStatus(modelStatus)

      log.info { "Mapped Solution Status $solutionStatus" }

      if (solutionStatus != LPSolutionStatus.OPTIMAL &&
        solutionStatus != LPSolutionStatus.TIME_LIMIT &&
        solutionStatus != LPSolutionStatus.CUTOFF
      ) {
        log.info { " No feasible solution" }
        model.solution = LPModelResult(solutionStatus)
        return solutionStatus
      }

      val resultStatus = extractVariableResults()
      if (!resultStatus) {
        log.error { "extractVariableResults() want true got false" }
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
        return LPSolutionStatus.ERROR
      }

      val objectiveVal = highsModel.getOutputInfo(HIGHSInfoParam.ObjectiveFunctionValue)
      val mipGap = highsModel.getOutputInfo(HIGHSInfoParam.MipGap)
      if (objectiveVal == null) {
        log.error { "Error while extracting the objective function value" }
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
        return LPSolutionStatus.ERROR
      }
      model.solution = LPModelResult(solutionStatus, objectiveVal.toDouble(), executionTime, mipGap?.toDouble())
      return solutionStatus
    } catch (e: Exception) {
      log.error { "Error while computing HIGHS model: $e" }
      model.solution = LPModelResult(LPSolutionStatus.ERROR)
      return LPSolutionStatus.ERROR
    } finally {
      highsModel.cleanup()
    }
  }

  private fun extractVariableResults(): Boolean {
    log.info { "Extracting results from the computed model result" }
    try {
      val res = highsModel.getSolution() ?: return false
      model.variables.allValues().forEach { lpVar ->
        val colIndex = variableMap[lpVar.identifier]!!
        val bounds = res.cols[colIndex]
        if (bounds == null) {
          log.error { " Bounds not found for variable $lpVar" }
          return false
        }
        lpVar.populateResult(bounds.primal)
      }
      return true
    } catch (e: Exception) {
      log.error { "Error while extracting results: $e" }
    }
    return false
  }

  internal fun getHighsVarType(varType: LPVarType): HIGHSVarType? =
    when (varType) {
      LPVarType.DOUBLE -> HIGHSVarType.CONTINUOUS
      LPVarType.BOOLEAN -> HIGHSVarType.INTEGER
      LPVarType.INTEGER -> HIGHSVarType.INTEGER
      else -> {
        null
      }
    }

  override fun initVars(): Boolean {
    log.info { "Initializing variables" }
    model.variables.allValues().forEach { lpVar ->
      try {
        log.debug { "Initializing variable ($lpVar)" }
        val highsVarType = getHighsVarType(lpVar.type)
        if (highsVarType == null) {
          log.error { "Could not determine var type for ${lpVar.type}" }
          return false
        }
        val highsVar = highsModel.createVar(lpVar.identifier, lpVar.lbound, lpVar.ubound, highsVarType)
        if (highsVar == null) {
          log.error {
            "createVar(${lpVar.identifier}, ${lpVar.lbound}, ${lpVar.ubound}, $highsVarType) got null want non null"
          }
          return false
        }
        variableMap[lpVar.identifier] = highsVar
      } catch (e: Exception) {
        log.error { "Exception while initializing variable $lpVar: $e" }
        return false
      }
    }
    log.info { "Variables initialized successfully" }
    return true
  }

  override fun initConstraints(): Boolean {
    log.info { "Initializing constraints" }
    model.constraints.allValues().forEach { lpConstraint ->
      try {
        log.info { "Initializing constraint $lpConstraint" }
        val reducedConstraint = model.reduce(lpConstraint)
        if (reducedConstraint == null) {
          log.error { "model.reduce($lpConstraint) want not null got null" }
          return false
        }
        val constraintBounds = generateConstraintBounds(reducedConstraint)
        if (constraintBounds == null) {
          log.error { "generateConstraintBounds($reducedConstraint) want not null got null" }
          return false
        }
        val constraint =
          highsModel.createConstraint(
            lpConstraint.identifier,
            constraintBounds.first,
            constraintBounds.second,
            reducedConstraint.lhs.expression
              .map { t ->
                Pair(variableMap[t.lpVarIdentifier!!]!!, t.coefficient!!)
              }.toList(),
          )
        if (constraint == null) {
          log.error { "createConstraint(${lpConstraint.identifier}...) want not null got null" }
          return false
        }
        constraintMap[lpConstraint.identifier] = constraint
      } catch (e: Exception) {
        log.error { "Error while generating constraint $lpConstraint: $e" }
        return false
      }
    }
    log.info { "Constraints initialized successfully" }
    return true
  }

  private fun generateConstraintBounds(reducedConstraint: LPConstraint): Pair<Double, Double>? {
    val rhsConstant =
      reducedConstraint.rhs.expression
        .stream()
        .filter { t -> t.isConstant() }
        .map { t -> t.coefficient!! }
        .findFirst()
        .orElse(0.0)
    return when (reducedConstraint.operator) {
      LPOperator.LESS_EQUAL -> Pair(highsModel.negInfinity(), rhsConstant)
      LPOperator.EQUAL -> Pair(rhsConstant, rhsConstant)
      LPOperator.GREATER_EQUAL -> Pair(rhsConstant, highsModel.infinity())
      else -> {
        log.error { "Cannot generate bounds for operator ${reducedConstraint.operator}" }
        return null
      }
    }
  }

  private fun getHIGHSObjective(objType: LPObjectiveType): HIGHSObjective =
    when (objType) {
      LPObjectiveType.MINIMIZE -> HIGHSObjective.MINIMIZE
      LPObjectiveType.MAXIMIZE -> HIGHSObjective.MAXIMIZE
    }

  override fun initObjectiveFunction(): Boolean {
    log.info { "Initializing Objective Function" }
    try {
      val reducedObjectiveFn = model.reduce(model.objective.expression)
      if (reducedObjectiveFn?.expression == null) {
        log.error { "reduced objective function want expression not null got null" }
        return false
      }
      reducedObjectiveFn.expression.forEach { term ->
        if (!term.isConstant()) {
          log.info { "Adding capability for term $term" }
          val highsVar = variableMap[term.lpVarIdentifier]
          if (highsVar == null) {
            log.error { "Found variable in objective ${term.lpVarIdentifier} that was not initialized" }
            return false
          }
          log.info { "Updating coefficient of variable ${term.lpVarIdentifier} to ${term.coefficient}" }
          highsModel.changeObjectiveCoefficient(highsVar, term.coefficient!!)
        } else {
          highsModel.changeObjectiveOffset(term.coefficient!!)
        }
      }
      highsModel.changeObjectiveDirection(getHIGHSObjective(model.objective.objective))
      log.info { "Objective Initialized" }
      return true
    } catch (e: Exception) {
      log.error { "Exception while configuring the SCIP objective function: $e" }
      return false
    }
  }
}