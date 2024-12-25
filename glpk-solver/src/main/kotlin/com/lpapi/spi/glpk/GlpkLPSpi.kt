package com.lpapi.spi.glpk

import com.lpapi.ffm.glpk.GLPKProblem
import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import com.lpapi.solver.glpk.GlpkLpSolver
import com.lpapi.spi.LPSpi

class GlpkLPSpi : LPSpi<GLPKProblem> {
  override fun create(model: LPModel): LPSolver<GLPKProblem> = GlpkLpSolver(model)
}