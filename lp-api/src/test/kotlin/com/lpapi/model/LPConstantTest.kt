package com.lpapi.model

import com.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPConstantTest : LPParameterTest<LPConstant>() {

  private val log = KotlinLogging.logger(LPConstantTest::javaClass.name)

  @Test
  @DisplayName("Test if equality conditions hold true")
  fun testEquality() {
    val c = LPConstant("c")
    log.info { "Testing with constant $c" }
    Assertions.assertEquals(c , c, "Same object instance should return true")
    Assertions.assertFalse(
      c.equals(null),
      "Equal comparison with a null value fails"
    )
    Assertions.assertFalse(
      c.equals(LPVar("c", LPVarType.BOOLEAN)),
      "Different param types  with same identifiers are not equal"
    )

    assertNotEquals(c, LPConstant("d"), "Constants with different identifiers are not equal")
    assertNotEquals(c, LPConstant("c", 1), "Constants with different values are not equal")
    c.value = 1.0
    assertEquals(c, LPConstant("c", 1), "Constants with same identifier and values are equal")
  }
}