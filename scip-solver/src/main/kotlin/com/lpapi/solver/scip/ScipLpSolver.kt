package com.lpapi.solver.scip

import com.lpapi.model.LPExpressionTerm
import com.lpapi.model.LPModel
import com.lpapi.model.LPModelResult
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType
import com.lpapi.solver.LPSolver
import jscip.*
import mu.KotlinLogging
import kotlin.system.measureTimeMillis


open class ScipLpSolver(model: LPModel): LPSolver<Scip>(model) {

  companion object {
    init {
      try {
        System.loadLibrary("jscip")
      } catch (e: Exception) {
        val log = KotlinLogging.logger(this::class.java.simpleName)

        /**
         * Information string.
         */
        var info =
            """
            The dynamic link library for GLPK for Java could not be loaded.
            Consider using java -Djava.library.path=
            The current value of system property java.library.path is:
            ${System.getProperty("java.library.path")}
          
          """

        info +=
            """    
          java.vendor: ${System.getProperty("java.vendor")}
          java.version: ${System.getProperty("java.version")}
          java.vm.name: ${System.getProperty("java.vm.name")}
          java.vm.version: ${System.getProperty("java.vm.version")}
          java.runtime.version: ${System.getProperty("java.runtime.version")}


          ${e.message}
          """
        log.error{ info }
        throw e
      }
    }
  }

  private var scipModel: Scip = Scip()

  private var variableMap: MutableMap<String, Variable> = mutableMapOf()

  private var constraintMap: MutableMap<String, Constraint> = mutableMapOf()

  override fun initModel(): Boolean {
    return try {
      scipModel.create(model.identifier)
      true
    } catch (e: Exception) {
      log.error { "Error while initializing Scip problem instance $e" }
      false
    }
  }

  override fun getBaseModel(): Scip? {
    return scipModel
  }

  private fun releaseModelVars() {
    log.info { "Releasing variables from SCIP" }
    variableMap.entries.forEach { p ->
      log.info { "Releasing variable ${p.key}" }
      scipModel.releaseVar(p.value)
    }
  }

  private fun releaseModelConstraints() {
    log.info { "Releasing constraints from SCIP" }
    constraintMap.entries.forEach { p ->
      log.info { "Releasing constraint ${p.key}" }
      scipModel.releaseCons(p.value)
    }
  }

  override fun solve(): LPSolutionStatus {
    try {
      // set parameters
      scipModel.setRealParam("limits/time", 100.0)
      scipModel.setRealParam("limits/memory", 10000.0)
      scipModel.setLongintParam("limits/totalnodes", 1000)
      scipModel.hideOutput(false)

      // Release variables and constraints as they are not required anymore

      //releaseModelVars() -- Once released the results for the individual variables cannot be extracted anymore
      releaseModelConstraints()
      // solve problem
      val executionTime = measureTimeMillis {
        scipModel.solve()
      }

      if (scipModel.sols?.size == 0) {
        log.info { "No feasible solutions found"}
        model.solution = LPModelResult(LPSolutionStatus.INFEASIBLE)
        return LPSolutionStatus.INFEASIBLE
      }

      val bestSol = scipModel.bestSol
      if (bestSol==null) {
        log.error { "Best solution not found" }
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
        return LPSolutionStatus.ERROR
      }
      if (!extractResults(bestSol)) {
        log.error { "Error while extracting results" }
        model.solution = LPModelResult(LPSolutionStatus.ERROR)
        return LPSolutionStatus.ERROR
      }

      val gap = scipModel.gap
      log.info { "final gap = $gap" }
      val objectiveVal = extractObjectiveVal()
      log.info { "Objective Val: $objectiveVal" }
      var solnStatus = LPSolutionStatus.CUTOFF
      if (gap == 0.0) {
        solnStatus = LPSolutionStatus.OPTIMAL
      }
      model.solution = LPModelResult(solnStatus, objectiveVal, executionTime, gap)
      return solnStatus
      // print all solutions
    } catch (e: Exception) {
      log.error { "Error while computing SCIP model: $e" }
      return LPSolutionStatus.ERROR
    }
  }

  private fun extractObjectiveVal(): Double {
    return model.objective.expression.expression.map( fun(t: LPExpressionTerm): Double {
      return if (t.isConstant()) {
        t.coefficient!!
      } else {
        t.coefficient!!.times(model.variables.get(t.lpVarIdentifier!!)!!.result.toDouble())
      }
    }).reduce{sum, element -> sum + element}
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

    model.variables.allValues().forEach{ lpVar ->
      try {
        log.debug { "Initializing variable ($lpVar)" }
        val scipVarType = getScipVarType(lpVar.type)
        if (scipVarType == null) {
          log.error { "Could not determine var type for ${lpVar.type}" }
          return false
        }
        val scipVar = scipModel.createVar(lpVar.identifier, lpVar.lbound, lpVar.ubound, 0.0, scipVarType)
        if (scipVar != null) {
          variableMap[lpVar.identifier] = scipVar
        } else {
          log.error { "Could not create variable for $lpVar" }
          return false
        }
      } catch (e: Exception) {
        log.error { "Error while initializing Scip Variable ($lpVar) : $e" }
        return false
      }
    }
    return true
  }

  override fun initConstraints(): Boolean {
    log.error { "Initializing constraints" }
    model.constraints.allValues().forEach { lpConstraint ->
      try {
        log.error { "Initializing constraint ($lpConstraint)" }
        val reducedConstraint = model.reduce(lpConstraint)
        if (reducedConstraint==null) {
          log.error { "Reduced constraint could not be computed for constraint ${lpConstraint.identifier}" }
          return false
        }
        log.error { "Reduced Constraint: $reducedConstraint" }
        val variables: MutableList<Variable> = mutableListOf()
        val coefficient: MutableList<Double> = mutableListOf()
        reducedConstraint.lhs.expression.filter { t -> !t.isConstant() && variableMap[t.lpVarIdentifier]!=null && t.coefficient != null }.map { t -> Pair(variableMap[t.lpVarIdentifier], t.coefficient) }.forEach {
          p ->
          variables.add(p.first!!)
          coefficient.add(p.second!!)
        }
        if (variables.size != coefficient.size || variables.size != reducedConstraint.lhs.expression.size) {
          log.error { "Inconsistent constraint initialization for LHS in reduced expression for constraints $lpConstraint" }
        }
        log.info { "Constraint variables ${variables} coefficients ${coefficient}" }
        val constant = reducedConstraint.rhs.expression.stream().filter { t -> t.isConstant() }.map { t -> t.coefficient!! }.findFirst().orElse(0.0)
        val bound: Pair<Double, Double> = when (reducedConstraint.operator) {
          LPOperator.LESS_EQUAL -> Pair(-1.0 * scipModel.infinity(), constant)
          LPOperator.EQUAL -> Pair(constant, constant)
          LPOperator.GREATER_EQUAL -> Pair(constant, scipModel.infinity())
        }
        log.error { "Bound for Constraints $bound" }
        val scipConstr = scipModel.createConsLinear(lpConstraint.identifier, variables.toTypedArray(), coefficient.toTypedArray().toDoubleArray(), bound.first, bound.second)
        if (scipConstr == null) {
          log.error { "Error while initializing SCIP constraint $lpConstraint" }
          return false
        }
        log.info { "Adding constraint $lpConstraint to the model" }
        scipModel.addCons(scipConstr)
        constraintMap[lpConstraint.identifier] = scipConstr
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
      if (reducedObjectiveFn?.expression==null) {
        return false
      }
      reducedObjectiveFn.expression.forEach { term ->
        if (!term.isConstant()) {
          val scipVar = variableMap[term.lpVarIdentifier]
          if (scipVar == null) {
            log.error { "Found variable in objective ${term.lpVarIdentifier} that was not initialized" }
            return false
          }
          log.info { "Updating coefficient of variable ${term.lpVarIdentifier} to ${term.coefficient}" }
          scipModel.changeVarObj(scipVar, term.coefficient!!)
        } else {
          log.error { "Constant parameter handling to be added to objective function by re-evaluating in result" }
        }
      }
      when (model.objective.objective) {
        LPObjectiveType.MAXIMIZE -> scipModel.setMaximize()
        LPObjectiveType.MINIMIZE -> scipModel.setMinimize()
      }
      return true
    } catch (e: Exception) {
      log.error { "Exception while configuring the SCIP objective function: $e" }
      return false
    }
  }

  internal fun getScipVarType(type: LPVarType): SCIP_Vartype? {
    return when(type) {
      LPVarType.INTEGER -> return SCIP_Vartype.SCIP_VARTYPE_INTEGER
      LPVarType.BOOLEAN -> return SCIP_Vartype.SCIP_VARTYPE_BINARY
      LPVarType.DOUBLE -> return SCIP_Vartype.SCIP_VARTYPE_CONTINUOUS
      else -> { null }
    }
  }
}