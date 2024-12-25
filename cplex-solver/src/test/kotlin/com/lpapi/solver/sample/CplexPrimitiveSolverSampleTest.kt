package com.lpapi.solver.sample

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.solver.cplex.CplexLpSolver
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CplexPrimitiveSolverSampleTest: PrimitiveSolverSample() {

  override fun initAndSolveModel(model: LPModel): LPModel? {
    val solver = CplexLpSolver(model)
    solver.initialize()
    val status = solver.solve()
    return if (status!= LPSolutionStatus.UNKNOWN &&
      status!= LPSolutionStatus.INFEASIBLE && status!= LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED)
      solver.model
    else
      null
  }

}


