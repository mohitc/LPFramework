package com.lpapi.solver.cplex

import com.lpapi.model.LPConstraint
import com.lpapi.model.LPExpression
import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPVarType
import com.lpapi.solver.LPSolver
import com.lpapi.model.enums.LPSolutionStatus
import ilog.concert.*
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

  override fun initObjectiveFunction(): Boolean {
    log.info { "Initializing Objective Function" }
    try {
      when(model.objective.objective) {
        LPObjectiveType.MAXIMIZE -> cplexModel?.maximize(generateExpression(model.objective.expression))
        LPObjectiveType.MINIMIZE -> cplexModel?.minimize(generateExpression(model.objective.expression))
        else -> {
          log.error { "Mechanism not implemented to support objective type ${model.objective.objective}" }
          null
        }
        //If objective if not created, return false otherwise return true
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
          log.error { "Reduced constraint could not be computed for constriat ${lpConstraint.identifier}" }
          return false
        }

        val modelConstraint : IloConstraint? =
          when(lpConstraint.operator) {
            LPOperator.LESS_EQUAL -> cplexModel?.addLe(generateExpression(lpConstraint.lhs), generateExpression(lpConstraint.rhs), lpConstraint.identifier)
            LPOperator.GREATER_EQUAL -> cplexModel?.addGe(generateExpression(lpConstraint.lhs), generateExpression(lpConstraint.rhs), lpConstraint.identifier)
            LPOperator.EQUAL -> cplexModel?.addEq(generateExpression(lpConstraint.lhs), generateExpression(lpConstraint.rhs), lpConstraint.identifier)
            else -> {null}
          }
        if (modelConstraint==null) {
          log.error { "Error while generating model constraint ${lpConstraint.identifier}" }
          return false
        }
        constraintMap.put(lpConstraint.identifier, modelConstraint)
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
  private fun generateExpression(expr: LPExpression) : IloLinearNumExpr ? {
    try {
      val cplexExpr: IloLinearNumExpr? = cplexModel?.linearNumExpr()
      if (cplexExpr == null) {
        log.error { "Unexpected error while generating expression. Check is model is initialized" }
        return null
      }
      val reducedExpr = model.reduce(expr)
      if (reducedExpr==null) {
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

  internal fun getCplexVarType(type: LPVarType) : IloNumVarType? {
    return when(type) {
      LPVarType.BOOLEAN -> IloNumVarType.Bool
      LPVarType.INTEGER -> IloNumVarType.Int
      LPVarType.DOUBLE -> IloNumVarType.Float
      else -> { null }
    }
  }
}