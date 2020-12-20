package com.lpapi.spi.glpk

import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import com.lpapi.solver.glpk.GlpkLpSolver
import com.lpapi.spi.LPSpi
import org.gnu.glpk.glp_prob

class GlpkLPSpi : LPSpi<glp_prob> {
  override fun create(model: LPModel): LPSolver<glp_prob> {
    return GlpkLpSolver(model)
  }
}