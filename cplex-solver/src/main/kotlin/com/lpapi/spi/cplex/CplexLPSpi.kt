package com.lpapi.spi.cplex

import com.lpapi.solver.LPSolver
import com.lpapi.solver.cplex.CplexLpSolver
import com.lpapi.spi.LPSpi
import ilog.cplex.IloCplex
import io.github.mohitc.lpapi.model.LPModel

class CplexLPSpi : LPSpi<IloCplex> {
  override fun create(model: LPModel): LPSolver<IloCplex> = CplexLpSolver(model)
}