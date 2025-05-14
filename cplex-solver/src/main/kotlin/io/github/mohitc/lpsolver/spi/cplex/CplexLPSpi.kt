package io.github.mohitc.lpsolver.spi.cplex

import ilog.cplex.IloCplex
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpsolver.LPSolver
import io.github.mohitc.lpsolver.cplex.CplexLpSolver
import io.github.mohitc.lpsolver.spi.LPSpi

class CplexLPSpi : LPSpi<IloCplex> {
  override fun create(model: LPModel): LPSolver<IloCplex> = CplexLpSolver(model)
}