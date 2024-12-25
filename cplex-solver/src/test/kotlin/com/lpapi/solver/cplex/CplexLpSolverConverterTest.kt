package com.lpapi.solver.cplex

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPVarType
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class CplexLpSolverConverterTest {

  @Test
  fun testGetCplexVarType() {
    val solver = CplexLpSolver(LPModel())
    LPVarType.values().forEach { v ->
      assertNotNull(solver.getCplexVarType(v), "No equivalent variable type associated with LPVarType $v")
    }
  }
}