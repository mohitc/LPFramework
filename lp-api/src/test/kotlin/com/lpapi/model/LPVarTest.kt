package com.lpapi.model

import com.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPVarTest : LPParameterTest<LPVar>() {
  private val log = KotlinLogging.logger(LPVarTest::javaClass.name)

  @Test
  @DisplayName("Test different constructions")
  fun testLpVarConstruction() {
    val a = LPVar("x", LPVarType.BOOLEAN)
    Assertions.assertEquals(a, a, "Same object instance should return true")
    Assertions.assertFalse(
      a == LPVar("y", LPVarType.BOOLEAN),
      "Variables with different identifiers should not be equal",
    )
    Assertions.assertFalse(
      a.equals(null),
      "Equal comparison with a null value fails",
    )
    Assertions.assertFalse(
      a.equals(LPConstant("x", 1)),
      "Different param types  with same identifiers are not equal",
    )
    a.bounds(0.0, 1.0)

    assertNotEquals(
      a,
      LPVar("x", LPVarType.DOUBLE, 0.0, 1.0),
      "Different variable types should not be equal",
    )
    assertNotEquals(
      a,
      LPVar("x", LPVarType.BOOLEAN, 0.5, 1.0),
      "Different variable types should not be equal",
    )
    assertNotEquals(
      a,
      LPVar("x", LPVarType.BOOLEAN, 0.0, 0.5),
      "Different variable types should not be equal",
    )
    val b = LPVar("x", LPVarType.BOOLEAN, 0.0, 1.0)
    assertEquals(a, b, "Variables with same types should be equal")
    b.result = 2
    assertNotEquals(a, b, "Variables with different result values are not equal")
    a.populateResult(2)
    assertNotEquals(
      a,
      b,
      "Variables with same result values but incorrect population are not equal",
    )
    b.populateResult(2)
    assertEquals(a, b, "Variables with same results and population mechanism are equal")
  }

  @Test
  @DisplayName("Test Result Population")
  fun testResultPopulation() {
    var x = LPVar("x", LPVarType.DOUBLE)
    log.info { "variable used $x" }
    Assertions.assertFalse(x.resultSet, "Variables should not have the result set by default")
    x.populateResult(1.5)
    Assertions.assertTrue(x.resultSet, "After populating result, resultSet should be true")
    Assertions.assertEquals(x.result, 1.5, "For double values, results should be stored as is")
    x = LPVar("x", LPVarType.INTEGER)
    x.populateResult(1.4)
    Assertions.assertNotEquals(x.result, 1.4, "For non-doubles, values should be rounded to the nearest int")
    Assertions.assertEquals(x.result, 1, "For non-doubles, values should be rounded to the nearest int")
    x.populateResult(1.6)
    Assertions.assertEquals(x.result, 2, "For non-doubles, values should be rounded to the nearest int")
    x = LPVar("x", LPVarType.BOOLEAN)
    x.populateResult(0.49)
    Assertions.assertNotEquals(x.result, 0.49, "For non-doubles, values should be rounded to the nearest int")
    Assertions.assertEquals(x.result, 0, "For non-doubles, values should be rounded to the nearest int")
    x.populateResult(0.51)
    Assertions.assertEquals(x.result, 1, "For non-doubles, values should be rounded to the nearest int")
  }
}