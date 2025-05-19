package io.github.mohitc.lpsolver.spi.ojalgo

import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpsolver.LPSolver
import io.github.mohitc.lpsolver.ojalgo.OjalgoLpSolver
import io.github.mohitc.lpsolver.spi.LPSpi
import org.ojalgo.optimisation.ExpressionsBasedModel

class OjalgoLPSpi : LPSpi<ExpressionsBasedModel> {
  override fun create(model: LPModel): LPSolver<ExpressionsBasedModel> = OjalgoLpSolver(model)
}