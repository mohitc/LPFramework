package com.lpapi.solver.cplex

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPVarType
import com.lpapi.solver.LPSolver
import com.lpapi.solver.enums.LPSolutionStatus
import ilog.concert.IloConstraint
import ilog.concert.IloNumVar
import ilog.concert.IloNumVarType
import ilog.cplex.IloCplex

class CplexLpSolver(model: LPModel): LPSolver<IloCplex>(model) {

  private var cplexModel: IloCplex? = null

  private var variableMap : MutableMap<String, IloNumVar> = mutableMapOf()

  private var constraintMap : MutableMap<String, IloConstraint> = mutableMapOf()


  override fun initModel(): Boolean {
    try{
      log.info { "Initializing cplex model for ${model.identifier}" }
      cplexModel = IloCplex()

    } catch (e: Exception) {
      log.error { "Error while initializing CPlex Model $e" }
      return false
    }
    return true
  }

  override fun getBaseModel(): IloCplex? {
    return cplexModel
  }

  override fun solve(): LPSolutionStatus {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  /** Function to initialize all variables in the model
   *
   */
  override fun initVars() : Boolean {
    log.info { "Initializing variables" }
    model.variables.allValues().forEach { lpVar ->
      try {
        log.debug { "Initializing variable ($lpVar)" }
        val cplexVar = cplexModel?.numVar(lpVar.lbound, lpVar.ubound, getCplexVarType(lpVar.type), lpVar.identifier)
        if (cplexVar != null) {
          variableMap[lpVar.identifier] = cplexVar
        }
      } catch (e: Exception) {
        log.error { "Error while initializing GLPK Var ($lpVar) : $e" }
        return false
      }
    }
    return true
  }

  override fun initConstraints(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun initObjectiveFunction(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  internal fun getCplexVarType(type: LPVarType) : IloNumVarType? {
    return when(type) {
      LPVarType.BOOLEAN -> IloNumVarType.Bool
      LPVarType.INTEGER -> IloNumVarType.Int
      LPVarType.DOUBLE -> IloNumVarType.Float
      else -> { null }
    }
  }
}