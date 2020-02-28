package com.lpapi.solver.gurobi

import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import com.lpapi.solver.enums.LPSolutionStatus
import gurobi.GRBEnv
import gurobi.GRBException
import gurobi.GRBModel
import mu.KotlinLogging

class GurobiLpSolver : LPSolver<GRBModel> {

  private val log = KotlinLogging.logger("GurobiLpSolver")

  private var model : GRBModel? = null

  override fun initialize(model: LPModel) {
    try {
      val env = GRBEnv()
      this.model = GRBModel(env)
//      initVars(model)
//      initConstraints(model)
    } catch (e: GRBException) {
      log.error("Error in generating Gurobi model", e)
    }
  }

  override fun getBaseModel(): GRBModel? {
    return model
  }

  override fun solve(): LPSolutionStatus {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}