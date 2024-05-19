package com.lpapi.solver.scip

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ScipLpSolverTest {
  private val log = KotlinLogging.logger { this.javaClass.name }

  @Test
  fun testScipVarTypeComputation() {
    val model = ScipLpSolver(LPModel())
    LPVarType.values().forEach { lpVarType ->
      assertNotNull(
          model.getScipVarType(lpVarType),
          "Scip Model variable type not found for LP " +
              "type $lpVarType"
      )
    }
  }
}