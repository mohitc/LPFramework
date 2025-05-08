package com.lpapi.spi.highs

import com.lpapi.ffm.highs.HIGHSProblem
import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import com.lpapi.solver.highs.HighsLPSolver
import com.lpapi.spi.LPSpi

class HighsLPSpi : LPSpi<HIGHSProblem> {
  override fun create(model: LPModel): LPSolver<HIGHSProblem> = HighsLPSolver(model)
}