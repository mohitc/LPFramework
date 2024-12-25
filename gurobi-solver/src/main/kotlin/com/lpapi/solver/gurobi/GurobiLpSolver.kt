package com.lpapi.solver.gurobi

import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import com.lpapi.solver.enums.LPSolutionStatus
import gurobi.GRBEnv
import gurobi.GRBException
import gurobi.GRBModel
import mu.KotlinLogging

class GurobiLpSolver(model: LPModel) : LPSolver<GRBModel>(model) {

  private val log = KotlinLogging.logger("GurobiLpSolver")

  private var grbModel : GRBModel? = null

  override fun initialize() : Boolean {
    try {
      val env = GRBEnv()
      this.grbModel = GRBModel(env)
//      initVars(model)
//      initConstraints(model)
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
}