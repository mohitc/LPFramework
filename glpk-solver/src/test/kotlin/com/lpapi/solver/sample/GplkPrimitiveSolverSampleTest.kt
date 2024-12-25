package com.lpapi.solver.sample

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.solver.glpk.GlpkLpSolver
import org.junit.jupiter.api.Tag

@Tag("integrationTest")
class GplkPrimitiveSolverSampleTest : PrimitiveSolverSample() {

  override fun initAndSolveModel(model: LPModel): LPModel? {
    val solver = GlpkLpSolver(model)
    solver.initialize()
    val status = solver.solve()
    return if (status!= LPSolutionStatus.UNKNOWN &&
        status!= LPSolutionStatus.INFEASIBLE && status!= LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED)
      solver.model
    else
      null
  }

}


