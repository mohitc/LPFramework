package io.github.mohitc.lpsolver.spi.glpk

import io.github.mohitc.glpk.ffm.GLPKProblem
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpsolver.LPSolver
import io.github.mohitc.lpsolver.glpk.GlpkLpSolver
import io.github.mohitc.lpsolver.spi.LPSpi

class GlpkLPSpi : LPSpi<GLPKProblem> {
  override fun create(model: LPModel): LPSolver<GLPKProblem> = GlpkLpSolver(model)
}