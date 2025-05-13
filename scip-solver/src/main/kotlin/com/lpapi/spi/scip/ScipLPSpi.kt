package com.lpapi.spi.scip

import com.lpapi.ffm.scip.SCIPProblem
import com.lpapi.solver.LPSolver
import com.lpapi.solver.scip.ScipLpSolver
import com.lpapi.spi.LPSpi
import io.github.mohitc.lpapi.model.LPModel

class ScipLPSpi : LPSpi<SCIPProblem> {
  override fun create(model: LPModel): LPSolver<SCIPProblem> = ScipLpSolver(model)
}