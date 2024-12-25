package com.lpapi.model

import com.lpapi.model.enums.LPSolutionStatus
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPModelResultTest {
  private val log = KotlinLogging.logger(LPModelResultTest::javaClass.name)

  @Test
  @DisplayName("Test different constructions")
  fun testEquality() {
    val assertTrue = fun(
      a: LPModelResult,
      b: LPModelResult,
      assertion: String,
    ) {
      Assertions.assertTrue(a == b, "LPModelResult.equals(): $assertion")
      Assertions.assertEquals(a.hashCode(), b.hashCode(), "LPModelResult.hashCode(): $assertion")
    }

    val assertFalse = fun(
      a: LPModelResult,
      b: LPModelResult,
      assertion: String,
    ) {
      Assertions.assertFalse(a == b, "LPModelResult.equals(): $assertion")
      Assertions.assertNotEquals(a.hashCode(), b.hashCode(), "LPModelResult.hashCode(): $assertion")
    }

    var result = LPModelResult(LPSolutionStatus.UNBOUNDED)
    assertTrue(result, result, "Same object instance should return true")
    Assertions.assertFalse(
      result.equals(LPConstant("x", 1)),
      "Different param types  with same identifiers are not equal",
    )

    result = LPModelResult(LPSolutionStatus.OPTIMAL, 1.0, 100, 2.0)
    log.info { "Testing equality for result $result" }

    // check that all values are populated correctly
    Assertions.assertTrue(
      result.status == LPSolutionStatus.OPTIMAL,
      "Status is populated correctly",
    )
    Assertions.assertTrue(
      result.objective == 1.0,
      "Objective is populated correctly",
    )
    Assertions.assertTrue(
      result.mipGap == 2.0,
      "MIP Gap is populated correctly",
    )
    Assertions.assertTrue(
      result.computationTime == 100L,
      "Computation time is populated correctly",
    )

    assertTrue(
      result,
      LPModelResult(LPSolutionStatus.OPTIMAL, 1.0, 100, 2.0),
      "If all parameters are equal, the model results are equal",
    )

    assertFalse(
      result,
      LPModelResult(LPSolutionStatus.TIME_LIMIT, 1.0, 100, 2.0),
      "Result with different solution status are not equal",
    )
    assertFalse(
      result,
      LPModelResult(LPSolutionStatus.OPTIMAL, 2.0, 100, 2.0),
      "Result with different objective are not equal",
    )
    assertFalse(
      result,
      LPModelResult(LPSolutionStatus.OPTIMAL, 1.0, 101, 2.0),
      "Result with different computation time are not equal",
    )
    assertFalse(
      result,
      LPModelResult(LPSolutionStatus.OPTIMAL, 1.0, 100, 2.1),
      "Result with different MIP gap are not equal",
    )
  }
}