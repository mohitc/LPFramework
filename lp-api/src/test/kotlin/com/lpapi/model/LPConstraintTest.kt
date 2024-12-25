package com.lpapi.model

import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPConstraintTest : LPParameterTest<LPConstraint>() {

  private val log = KotlinLogging.logger(LPConstraintTest::javaClass.name)

  @Test
  @DisplayName("Test if equality conditions hold true")
  fun testEquality() {
    // define expressions and constraints for test
    val lhs = LPExpression()
    lhs.addTerm("a", "x").addTerm("b", "y")
    val rhs = LPExpression()
    rhs.add("c")
    val c = LPConstraint("constraint")
    c.lhs.expression = lhs.expression
    c.rhs.expression = rhs.expression
    log.info { "Testing with constant $c" }
    Assertions.assertTrue(c == c, "Same object instance should return true")
    Assertions.assertFalse(
      c.equals(null),
      "Equal comparison with a null value fails"
    )
    Assertions.assertFalse(
      c.equals(LPVar("constraint", LPVarType.BOOLEAN)),
      "Different param types  with same identifiers are not equal"
    )

    assertEquals(
      c, LPConstraint("constraint", lhs, LPOperator.GREATER_EQUAL, rhs),
      "Default operation in the constructor is greater equal"
    )
    assertNotEquals(
      c, LPConstraint("not-constraint", lhs, LPOperator.GREATER_EQUAL, rhs),
      "Constraints with different identifiers are not equal"
    )
    assertNotEquals(
      c, LPConstraint("constraint", LPExpression(), LPOperator.GREATER_EQUAL, rhs),
      "Constraints with different LHS are not equal"
    )
    assertNotEquals(
      c, LPConstraint("constraint", lhs, LPOperator.EQUAL, rhs),
      "Constraints with different operators are not equal"
    )
    assertNotEquals(
      c, LPConstraint("constraint", LPExpression(), LPOperator.GREATER_EQUAL, LPExpression()),
      "Constraints with different RHS are not equal"
    )
    c.operator = LPOperator.EQUAL
    assertEquals(
      c, LPConstraint("constraint", lhs, LPOperator.EQUAL, rhs),
      "Setting the operator changes the constraint"
    )
  }
}