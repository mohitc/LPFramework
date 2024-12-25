package com.lpapi.solver.sample

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.solver.gurobi.GurobiLpSolver

class GurobiPrimititiveSolverSampleTest : PrimitiveSolverSample() {

  override fun initAndSolveModel(model: LPModel): LPModel? {
    val solver = GurobiLpSolver(model)
    solver.initialize()
    val status = solver.solve()
    return if (status!= LPSolutionStatus.UNKNOWN &&
        status!= LPSolutionStatus.INFEASIBLE && status!= LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED)
      solver.model
    else
      null
  }

}


