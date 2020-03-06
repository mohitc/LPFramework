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
    if (!model.validate()) {
      log.error { "Model could not be validated " }
      return null
    }
    val solver = CplexLpSolver(model)
    solver.initialize()
    val status = solver.solve()
    return if (status!= LPSolutionStatus.UNKNOWN &&
      status!= LPSolutionStatus.INFEASIBLE && status!= LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED)
      solver.model
    else
      null
  }

  @Test
  fun testSolver() {
    val model = initAndSolveModel(initLpModel())
    assertNotNull(model, "Model should be computed successfully.")
    val objectiveVal = model.solution?.objective ?: 0.0
    assertTrue(abs(objectiveVal - 2.0) <= 1E-5, "Objective function should be = 2")
    assertEquals(model.variables.get("X")?.result, 1, "X should be = 1")
    assertEquals(model.variables.get("Y")?.result, 1, "Y should be = 1")
    assertEquals(model.variables.get("Z")?.result, 0, "Z should be = 0")
  }

}


