package io.github.mohitc.lpsolver.spi.scip

import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpsolver.LPSolver
import io.github.mohitc.lpsolver.scip.ScipLpSolver
import io.github.mohitc.lpsolver.spi.LPSpi
import io.github.mohitc.scip.ffm.SCIPProblem

class ScipLPSpi : LPSpi<SCIPProblem> {
  override fun create(model: LPModel): LPSolver<SCIPProblem> = ScipLpSolver(model)
}