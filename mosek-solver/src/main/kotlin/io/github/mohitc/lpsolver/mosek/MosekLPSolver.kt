package io.github.mohitc.lpsolver.mosek

import com.mosek.mosek.Stream
import com.mosek.mosek.Task
import com.mosek.mosek.boundkey
import com.mosek.mosek.objsense
import com.mosek.mosek.solsta
import com.mosek.mosek.soltype
import com.mosek.mosek.streamtype
import com.mosek.mosek.variabletype
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPModelResult
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import io.github.mohitc.lpsolver.LPSolver
import kotlin.system.measureTimeMillis

class MosekLPSolver(
  model: LPModel,
) : LPSolver<Task>(model) {
  private var baseModel: Task? = null

  private val variableMap: MutableMap<String, Int> = mutableMapOf<String, Int>()

  private val constraintMap: MutableMap<String, Int> = mutableMapOf<String, Int>()

  override fun initModel(): Boolean {
    try {
      val task = Task()
      // route log stream to the slf4j logger
      task.set_Stream(
        streamtype.log,
        object : Stream() {
          override fun stream(msg: String) = log.info { msg }
        },
      )
      baseModel = task
    } catch (e: Throwable) {
      log.error { "Exception while initializing model: $e" }
      log.debug { e.stackTraceToString() }
      return false
    }
    return true
  }

  override fun getBaseModel(): Task? = baseModel

  override fun solve(): LPSolutionStatus {
    try {
      if (baseModel == null) {
        log.error("Base model not initialized.")
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
        return LPSolutionStatus.ERROR
      }

      // Solve the problem
      val executionTime =
        measureTimeMillis {
          baseModel!!.optimize()
        }
      // Print a summary containing information
      // about the solution for debugging purposes
      baseModel!!.solutionsummary(streamtype.msg)

      // Get status information about the solution
      val mosekStatus = baseModel!!.getsolsta(soltype.itg)

      val solutionStatus = convertSolutionStatus(mosekStatus)

      when (solutionStatus) {
        LPSolutionStatus.ERROR, LPSolutionStatus.INFEASIBLE, LPSolutionStatus.UNBOUNDED,
        LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED,
        ->
          model.solution = LPModelResult(solutionStatus)
        else -> {
          val variableResults = baseModel!!.getxx(soltype.itg)
          variableMap.forEach {
            lpVarIdentifier,
            index,
            ->
            model.variables.get(lpVarIdentifier)?.populateResult(variableResults[index])
          }
          val objectiveVal = model.evaluate(model.objective.expression)
          if (objectiveVal == null) {
            log.error { "evaluate(${model.objective.expression}) want non-null got null" }
            model.solution = LPModelResult(LPSolutionStatus.ERROR)
            return LPSolutionStatus.ERROR
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
        }
      }
      return solutionStatus
    } catch (e: Throwable) {
      log.error { "Exception while solving the model: $e" }
      log.debug { e.stackTraceToString() }

      model.solution = LPModelResult(LPSolutionStatus.ERROR)
      return LPSolutionStatus.ERROR
    }
  }

  fun convertSolutionStatus(status: solsta): LPSolutionStatus =
    when (status) {
      solsta.dual_feas -> LPSolutionStatus.CUTOFF
      solsta.dual_infeas_cer -> LPSolutionStatus.INFEASIBLE
      solsta.integer_optimal -> LPSolutionStatus.OPTIMAL
      solsta.optimal -> LPSolutionStatus.OPTIMAL
      solsta.prim_and_dual_feas -> LPSolutionStatus.CUTOFF
      solsta.prim_feas -> LPSolutionStatus.CUTOFF
      solsta.prim_infeas_cer -> LPSolutionStatus.INFEASIBLE
      solsta.unknown -> LPSolutionStatus.UNKNOWN
      else -> LPSolutionStatus.UNKNOWN
    }

  private fun convertVarType(lpVarType: LPVarType): variabletype =
    when (lpVarType) {
      LPVarType.BOOLEAN -> variabletype.type_int
      LPVarType.INTEGER -> variabletype.type_int
      LPVarType.DOUBLE -> variabletype.type_cont
    }

  override fun initVars(): Boolean {
    log.info { "Initializing Variables" }
    val numVar = model.variables.allValues().size
    log.info { "Variable Size = $numVar" }
    var currVarIdentifier = 0
    try {
      baseModel?.appendvars(numVar)
      model.variables.allValues().forEach { lpVar ->
        log.info { "Initializing variable $lpVar" }
        baseModel!!.putvarname(currVarIdentifier, lpVar.identifier)
        baseModel!!.putvarbound(currVarIdentifier, boundkey.ra, lpVar.lbound, lpVar.ubound)
        baseModel!!.putvartype(currVarIdentifier, convertVarType(lpVar.type))
        variableMap[lpVar.identifier] = currVarIdentifier
        currVarIdentifier++
      }
    } catch (e: Throwable) {
      log.error { "Error while initializing variables: $e" }
      log.debug { e.stackTraceToString() }
      return false
    }
    return true
  }

  override fun initConstraints(): Boolean {
    val numConstraints = model.constraints.allValues().size
    // we need to store the contribution of a variable to a constraint for initializing
    // the mosek model. In the first pass, we store the contributions as a pair of Int
    // (constraint identifier) and the contribution (double) for each variable, and once
    // all constraints are mapped to an identifier, we set the bounds in the mosek model.
    val varContributions = mutableMapOf<String, MutableList<Pair<Int, Double>>>()
    var currentConstraintIdentifier = 0
    try {
      baseModel?.appendcons(numConstraints)
      model.constraints.allValues().forEach { lPConstraint ->
        log.debug { "Initializing constraint: $lPConstraint" }
        val reducedConstraint = model.reduce(lPConstraint)
        if (reducedConstraint == null) {
          log.error { "empty value while reducing constraint $lPConstraint" }
          return false
        }
        constraintMap[lPConstraint.identifier] = currentConstraintIdentifier
        reducedConstraint.lhs.expression.forEach { t ->
          if (t.isConstant()) {
            log.error { "Constant term not expected in the LHS of a reduced constraint $reducedConstraint" }
            return false
          }
          if (varContributions[t.lpVarIdentifier!!] == null) {
            varContributions[t.lpVarIdentifier!!] = mutableListOf()
          }
          // Initialize variable contributions for each term in the constraint
          varContributions[t.lpVarIdentifier]!!.add(Pair(currentConstraintIdentifier, t.coefficient!!))
        }
        val value =
          reducedConstraint.rhs.expression
            .filter { t ->
              t.isConstant()
            }.map { t -> t.coefficient }
            .firstOrNull() ?: 0.0
        when (reducedConstraint.operator) {
          LPOperator.EQUAL -> baseModel!!.putconbound(currentConstraintIdentifier, boundkey.fx, value, value)
          LPOperator.LESS_EQUAL -> baseModel!!.putconbound(currentConstraintIdentifier, boundkey.up, 0.0, value)
          LPOperator.GREATER_EQUAL -> baseModel!!.putconbound(currentConstraintIdentifier, boundkey.lo, value, 0.0)
        }
        currentConstraintIdentifier++
      }
      log.info { "Variable Map: $variableMap" }
      log.info { "Variable Contributions: $varContributions" }
      // Initialize all variable contributions
      varContributions.forEach { varIdentifier, coeffList ->
        baseModel!!.putacol(
          variableMap[varIdentifier]!!,
          coeffList
            .map { t ->
              t.first
            }.toIntArray(),
          coeffList.map { t -> t.second }.toDoubleArray(),
        )
      }
    } catch (e: Throwable) {
      log.error { "Error while initializing constraints: $e" }
      log.debug { e.stackTraceToString() }
      return false
    }
    return true
  }

  private fun getObjectiveSense(obj: LPObjectiveType): objsense =
    when (obj) {
      LPObjectiveType.MAXIMIZE -> objsense.maximize
      LPObjectiveType.MINIMIZE -> objsense.minimize
    }

  override fun initObjectiveFunction(): Boolean {
    try {
      val reducedObjective =
        model
          .reduce(model.objective.expression)
      reducedObjective?.expression?.forEach { t ->
        if (t.isConstant()) {
          log.debug {
            "Constant terms are not supported in mosek model. Will include them in the " + "" +
              "recalculation of the objective value calculation only"
          }
        } else {
          baseModel!!.putcj(variableMap[t.lpVarIdentifier]!!, t.coefficient!!)
        }
      }
      baseModel!!.putobjsense(getObjectiveSense(model.objective.objective))
    } catch (e: Throwable) {
      log.error { "Error while initializing objective function: $e" }
      log.debug { e.stackTraceToString() }
      return false
    }
    return true
  }
}