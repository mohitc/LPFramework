package com.lpapi.spi.glpk

import com.lpapi.ffm.glpk.GLPKProblem
import com.lpapi.solver.LPSolver
import com.lpapi.solver.glpk.GlpkLpSolver
import com.lpapi.spi.LPSpi
import io.github.mohitc.lpapi.model.LPModel

class GlpkLPSpi : LPSpi<GLPKProblem> {
  override fun create(model: LPModel): LPSolver<GLPKProblem> = GlpkLpSolver(model)
}