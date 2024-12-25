package com.lpapi.spi.scip

import com.lpapi.ffm.scip.SCIPProblem
import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import com.lpapi.solver.scip.ScipLpSolver
import com.lpapi.spi.LPSpi

class ScipLPSpi : LPSpi<SCIPProblem> {
  override fun create(model: LPModel): LPSolver<SCIPProblem> = ScipLpSolver(model)
}