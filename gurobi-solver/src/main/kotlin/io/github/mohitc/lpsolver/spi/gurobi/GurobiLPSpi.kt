package io.github.mohitc.lpsolver.spi.gurobi

import com.gurobi.gurobi.GRBModel
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpsolver.LPSolver
import io.github.mohitc.lpsolver.gurobi.GurobiLpSolver
import io.github.mohitc.lpsolver.spi.LPSpi

class GurobiLPSpi : LPSpi<GRBModel> {
  override fun create(model: LPModel): LPSolver<GRBModel> = GurobiLpSolver(model)
}