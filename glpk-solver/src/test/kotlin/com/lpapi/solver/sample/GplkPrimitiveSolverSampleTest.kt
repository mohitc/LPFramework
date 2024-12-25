package com.lpapi.solver.sample

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.solver.glpk.GlpkLpSolver
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Tag("integrationTest")
class GplkPrimitiveSolverSampleTest : PrimitiveSolverSample() {

  override fun initAndSolveModel(model: LPModel): LPModel? {
    if (!model.validate()) {
      log.error { "Model could not be validated " }
      return null
    }
    val solver = GlpkLpSolver(model)
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
    assertEquals(model.variables.get("X")?.result, 1, "X should be = 1")
    assertEquals(model.variables.get("Y")?.result, 1, "Y should be = 1")
    assertEquals(model.variables.get("Z")?.result, 0, "Z should be = 0")
  }

}


