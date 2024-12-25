package com.lpapi.spi.scip

import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import com.lpapi.solver.scip.ScipLpSolver
import com.lpapi.spi.LPSpi
import jscip.Scip

class ScipLPSpi : LPSpi<Scip> {
  override fun create(model: LPModel): LPSolver<Scip> {
    return ScipLpSolver(model)
  }
}