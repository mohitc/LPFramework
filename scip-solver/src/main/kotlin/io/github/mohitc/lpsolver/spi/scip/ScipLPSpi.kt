package io.github.mohitc.lpsolver.spi.scip

import com.lpapi.ffm.scip.SCIPProblem
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpsolver.LPSolver
import io.github.mohitc.lpsolver.scip.ScipLpSolver
import io.github.mohitc.lpsolver.spi.LPSpi

class ScipLPSpi : LPSpi<SCIPProblem> {
  override fun create(model: LPModel): LPSolver<SCIPProblem> = ScipLpSolver(model)
}