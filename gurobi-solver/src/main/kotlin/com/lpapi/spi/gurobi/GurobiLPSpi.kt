package com.lpapi.spi.gurobi

import com.gurobi.gurobi.GRBModel
import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import com.lpapi.solver.gurobi.GurobiLpSolver
import com.lpapi.spi.LPSpi

class GurobiLPSpi : LPSpi<GRBModel> {
  override fun create(model: LPModel): LPSolver<GRBModel> = GurobiLpSolver(model)
}