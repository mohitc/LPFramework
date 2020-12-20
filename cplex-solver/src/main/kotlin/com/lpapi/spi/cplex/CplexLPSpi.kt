package com.lpapi.spi.cplex

import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import com.lpapi.solver.cplex.CplexLpSolver
import com.lpapi.spi.LPSpi
import ilog.cplex.IloCplex

class CplexLPSpi : LPSpi<IloCplex> {
  override fun create(model: LPModel): LPSolver<IloCplex> {
    return CplexLpSolver(model)
  }
}