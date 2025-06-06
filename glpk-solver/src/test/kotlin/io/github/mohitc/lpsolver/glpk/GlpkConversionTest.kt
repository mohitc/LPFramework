package io.github.mohitc.lpsolver.glpk

import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.enums.LPVarType
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class GlpkConversionTest {
  @Test
  fun testVarTypeConversion() {
    val model = LPModel()
    val solver = GlpkLpSolver(model)
    LPVarType.values().forEach { v ->
      assertNotNull(solver.getGlpVarType(v))
    }
  }
}