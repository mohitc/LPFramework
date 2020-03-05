package com.lpapi.solver.gurobi

import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import com.lpapi.model.enums.LPSolutionStatus
import gurobi.GRBEnv
import gurobi.GRBException
import gurobi.GRBModel

class GurobiLpSolver(model: LPModel) : LPSolver<GRBModel>(model) {

  private var grbModel : GRBModel? = null

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

  override fun getBaseModel(): GRBModel? {
    return grbModel
  }

  override fun solve(): LPSolutionStatus {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun initVars(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun initConstraints(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun initObjectiveFunction(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}