package io.github.mohitc.lpsolver.scip

import io.github.mohitc.lpapi.model.LPExpressionTerm
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPModelResult
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import io.github.mohitc.lpsolver.LPSolver
import io.github.mohitc.scip.ffm.Constraint
import io.github.mohitc.scip.ffm.SCIPProblem
import io.github.mohitc.scip.ffm.SCIPRetCode
import io.github.mohitc.scip.ffm.SCIPStatus
import io.github.mohitc.scip.ffm.SCIPVarType
import io.github.mohitc.scip.ffm.Solution
import io.github.mohitc.scip.ffm.Variable
import kotlin.system.measureTimeMillis

open class ScipLpSolver(
  model: LPModel,
) : LPSolver<SCIPProblem>(model) {
  companion object {
    internal fun getSolutionStatus(status: SCIPStatus): LPSolutionStatus =
      when (status) {
        SCIPStatus.OPTIMAL -> LPSolutionStatus.OPTIMAL
        SCIPStatus.INFEASIBLE -> LPSolutionStatus.INFEASIBLE
        SCIPStatus.UNBOUNDED -> LPSolutionStatus.UNBOUNDED
        SCIPStatus.TIME_LIMIT -> LPSolutionStatus.TIME_LIMIT
        SCIPStatus.INFEASIBLE_OR_UNBOUNDED -> LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED
        SCIPStatus.SOLUTION_LIMIT,
        SCIPStatus.GAP_LIMIT,
        SCIPStatus.BEST_SOLUTION_LIMIT,
        SCIPStatus.NODE_LIMIT,
        SCIPStatus.TOTAL_NODE_LIMIT,
        SCIPStatus.STALL_NODE_LIMIT,
        SCIPStatus.DUAL_LIMIT,
        SCIPStatus.MEMORY_LIMIT,
        SCIPStatus.PRIMAL_LIMIT,
        SCIPStatus.RESTART_LIMIT,
        -> LPSolutionStatus.CUTOFF
        SCIPStatus.UNKNOWN -> LPSolutionStatus.UNKNOWN
        SCIPStatus.TERMINATED -> LPSolutionStatus.ERROR
        SCIPStatus.USER_INTERRUPT -> LPSolutionStatus.ERROR
      }

//    init {
//      try {
//        System.loadLibrary("scip")
//      } catch (e: Exception) {
//        val log = KotlinLogging.logger(this::class.java.simpleName)
//
//        /**
//         * Information string.
//         */
//        var info =
//            """
//            The dynamic link library for SCIP could not be loaded.
//            Consider using java -Djava.library.path=
//            The current value of system property java.library.path is:
//            ${System.getProperty("java.library.path")}
//
//          """
//
//        info +=
//            """
//          java.vendor: ${System.getProperty("java.vendor")}
//          java.version: ${System.getProperty("java.version")}
//          java.vm.name: ${System.getProperty("java.vm.name")}
//          java.vm.version: ${System.getProperty("java.vm.version")}
//          java.runtime.version: ${System.getProperty("java.runtime.version")}
//
//
//          ${e.message}
//          """
//        log.error { info }
//        throw e
//      }
//    }
  }

  private var scipModel: SCIPProblem = SCIPProblem()

  private var variableMap: MutableMap<String, Variable> = mutableMapOf()

  private var constraintMap: MutableMap<String, Constraint> = mutableMapOf()

  override fun initModel(): Boolean {
    try {
      val createVarRetVal = scipModel.createProblem(model.identifier)
      log.info { "createProblem(${model.identifier}) = $createVarRetVal" }
      val includePluginsRetVal = scipModel.includeDefaultPlugins()
      log.info { "includeDefaultPlugins() = $includePluginsRetVal" }
      return createVarRetVal == SCIPRetCode.SCIP_OKAY && includePluginsRetVal == SCIPRetCode.SCIP_OKAY
    } catch (e: Exception) {
      log.error { "Error while initializing Scip problem instance $e" }
    }
    return false
  }

  override fun getBaseModel() = scipModel

  private fun releaseModelVars() {
    log.info { "Releasing variables from SCIP" }
    variableMap.entries.forEach { p ->
      log.debug { "Releasing variable ${p.key}" }
      val retCode = scipModel.releaseVar(p.value)
      if (retCode != SCIPRetCode.SCIP_OKAY) {
        log.error { "releaseVar(${p.value}) want OKAY got $retCode" }
      }
    }
  }

  private fun releaseModelConstraints() {
    log.info { "Releasing constraints from SCIP" }
    constraintMap.entries.forEach { p ->
      log.debug { "Releasing constraint ${p.key}" }
      val retCode = scipModel.releaseConstraint(p.value)
      if (retCode != SCIPRetCode.SCIP_OKAY) {
        log.error { "releaseVar(${p.value}) want OKAY got $retCode" }
      }
    }
  }

  override fun solve(): LPSolutionStatus {
    try {
      // set parameters
      var retCode = scipModel.setRealParam("limits/time", 100.0)
      if (retCode != SCIPRetCode.SCIP_OKAY) {
        log.error { "setting time limits want OKAY got $retCode" }
      }
      retCode = scipModel.setRealParam("limits/memory", 10000.0)
      if (retCode != SCIPRetCode.SCIP_OKAY) {
        log.error { "setting memory limits want OKAY got $retCode" }
      }
      retCode = scipModel.setLongintParam("limits/totalnodes", 1000)
      if (retCode != SCIPRetCode.SCIP_OKAY) {
        log.error { "setting total node limits want OKAY got $retCode" }
      }
      scipModel.messageHandlerQuiet(false)

      releaseModelConstraints()
      // solve problem
      val executionTime =
        measureTimeMillis {
          retCode = scipModel.solve()
          log.info { "model.solve() return code $retCode" }
        }

      val bestSol = scipModel.getBestSol()
      if (bestSol == null) {
        log.error { "Best solution not found" }
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
        return LPSolutionStatus.ERROR
      }
      if (!extractResults(bestSol)) {
        log.error { "Error while extracting results" }
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
        return LPSolutionStatus.ERROR
      }

      val gap = scipModel.getGap()
      log.info { "final gap = $gap" }
      val objectiveVal = extractObjectiveVal()
      log.info { "Objective Val: $objectiveVal" }

      val solutionStatus = getSolutionStatus(scipModel.getStatus())
      model.solution = LPModelResult(solutionStatus, objectiveVal, executionTime, gap)
      releaseModelVars()
      retCode = scipModel.freeProblem()
      if (retCode != SCIPRetCode.SCIP_OKAY) {
        log.error { "freeProblem() want OKAY got $retCode" }
      }
      return solutionStatus
      // print all solutions
    } catch (e: Exception) {
      log.error { "Error while computing SCIP model: $e" }
      return LPSolutionStatus.ERROR
    }
  }

  private fun extractObjectiveVal(): Double {
    if (model.objective.expression.expression
        .isEmpty()
    ) {
      return 0.0
    }
    return model.objective.expression.expression
      .map(
        fun(t: LPExpressionTerm): Double =
          if (t.isConstant()) {
            t.coefficient!!
          } else {
            t.coefficient!!.times(
              model.variables
                .get(t.lpVarIdentifier!!)!!
                .result
                .toDouble(),
            )
          },
      ).reduce { sum, element -> sum + element }
  }

  private fun extractResults(sol: Solution): Boolean {
    log.info { "Extracting results of the computed model into the variables" }
    for (entry in variableMap.entries) {
      val res = scipModel.getSolVal(sol, entry.value)
      log.info { "Variable ${entry.key} value $res" }
      model.variables.get(entry.key)?.populateResult(res)
    }
    return true
  }

  override fun initVars(): Boolean {
    log.info { "Initializing variables" }

    model.variables.allValues().forEach { lpVar ->
      try {
        log.debug { "Initializing variable ($lpVar)" }
        val scipVarType = getScipVarType(lpVar.type)
        val scipVar = scipModel.createVar(lpVar.identifier, lpVar.lbound, lpVar.ubound, 0.0, scipVarType)
        if (scipVar != null) {
          variableMap[lpVar.identifier] = scipVar
        } else {
          log.error { "Could not create variable for $lpVar" }
          return false
        }
        log.info { "Variable $lpVar created successfully" }
      } catch (e: Exception) {
        log.error { "Error while initializing Scip Variable ($lpVar) : $e" }
        return false
      }
    }
    log.info { "Variables initialized successfully" }
    return true
  }

  override fun initConstraints(): Boolean {
    log.error { "Initializing constraints" }
    model.constraints.allValues().forEach { lpConstraint ->
      try {
        log.info { "Initializing constraint (${lpConstraint.identifier})" }
        val reducedConstraint = model.reduce(lpConstraint)
        if (reducedConstraint == null) {
          log.error { "Reduced constraint could not be computed for constraint ${lpConstraint.identifier}" }
          return false
        }
        val variables: MutableList<Variable> = mutableListOf()
        val coefficient: MutableList<Double> = mutableListOf()
        reducedConstraint.lhs.expression
          .filter { t ->
            !t.isConstant() &&
              variableMap[t.lpVarIdentifier] != null &&
              t.coefficient != null
          }.map { t -> Pair(variableMap[t.lpVarIdentifier], t.coefficient) }
          .forEach { p ->
            variables.add(p.first!!)
            coefficient.add(p.second!!)
          }
        if (variables.size != coefficient.size || variables.size != reducedConstraint.lhs.expression.size) {
          log.error {
            "Inconsistent constraint initialization for LHS in reduced expression for constraints $lpConstraint"
          }
        }
        log.debug { "Constraint variables $variables coefficients $coefficient" }
        val constant =
          reducedConstraint.rhs.expression
            .stream()
            .filter { t ->
              t.isConstant()
            }.map { t -> t.coefficient!! }
            .findFirst()
            .orElse(0.0)
        val bound: Pair<Double, Double> =
          when (reducedConstraint.operator) {
            LPOperator.LESS_EQUAL -> Pair(-1.0 * scipModel.infinity(), constant)
            LPOperator.EQUAL -> Pair(constant, constant)
            LPOperator.GREATER_EQUAL -> Pair(constant, scipModel.infinity())
          }
        log.debug { "Bound for Constraints $bound" }
        val scipConstraint =
          scipModel.createConstraint(
            lpConstraint.identifier,
            variables,
            coefficient,
            bound.first,
            bound.second,
          )
        if (scipConstraint == null) {
          log.error { "Error while initializing SCIP constraint $lpConstraint" }
          return false
        }
        constraintMap[lpConstraint.identifier] = scipConstraint
      } catch (e: Exception) {
        log.error { "Error while initializing constraint $lpConstraint : $e" }
        return false
      }
    }
    return true
  }

  override fun initObjectiveFunction(): Boolean {
    log.info { "Initializing Objective Function" }
    try {
      val reducedObjectiveFn = model.reduce(model.objective.expression)
      log.debug { "Reduced Objective Function: $reducedObjectiveFn" }
      if (reducedObjectiveFn?.expression == null) {
        return false
      }
      reducedObjectiveFn.expression.forEach { term ->
        if (!term.isConstant()) {
          log.debug { "Adding capability for term $term" }
          val scipVar = variableMap[term.lpVarIdentifier]
          if (scipVar == null) {
            log.error { "Found variable in objective ${term.lpVarIdentifier} that was not initialized" }
            return false
          }
          log.info { "Updating coefficient of variable ${term.lpVarIdentifier} to ${term.coefficient}" }
          scipModel.setVariableObjective(scipVar, term.coefficient!!)
        } else {
          log.error { "Constant parameter handling to be added to objective function by re-evaluating in result" }
        }
      }
      when (model.objective.objective) {
        LPObjectiveType.MAXIMIZE -> scipModel.maximize()
        LPObjectiveType.MINIMIZE -> scipModel.minimize()
      }
      log.info { "Objective Initialized" }
      return true
    } catch (e: Exception) {
      log.error { "Exception while configuring the SCIP objective function: $e" }
      return false
    }
  }

  internal fun getScipVarType(type: LPVarType): SCIPVarType =
    when (type) {
      LPVarType.INTEGER -> SCIPVarType.SCIP_VARTYPE_INTEGER
      LPVarType.BOOLEAN -> SCIPVarType.SCIP_VARTYPE_BINARY
      LPVarType.DOUBLE -> SCIPVarType.SCIP_VARTYPE_CONTINUOUS
    }
}