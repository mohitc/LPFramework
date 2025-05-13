package io.github.mohitc.lpsolver.spi.highs

import com.lpapi.ffm.highs.HIGHSProblem
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpsolver.LPSolver
import io.github.mohitc.lpsolver.highs.HighsLPSolver
import io.github.mohitc.lpsolver.spi.LPSpi

class HighsLPSpi : LPSpi<HIGHSProblem> {
  override fun create(model: LPModel): LPSolver<HIGHSProblem> = HighsLPSolver(model)
}