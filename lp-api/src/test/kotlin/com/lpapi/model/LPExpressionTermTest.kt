package com.lpapi.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class LPExpressionTermTest {
  /** function to test if a term is a constant */
  @Test
  @DisplayName("Test to check if the isConstant method identifies terms correctly")
  fun testIsConstant() {
    assertTrue(
      LPExpressionTerm(coefficient = 1.0, lpConstantIdentifier = null, lpVarIdentifier = null).isConstant(),
      "Term with only the coefficient should be constant",
    )
    assertTrue(
      LPExpressionTerm(coefficient = null, lpConstantIdentifier = "c", lpVarIdentifier = null).isConstant(),
      "Term with only the constant identifier should be constant",
    )
    assertFalse(
      LPExpressionTerm(coefficient = 1.0, lpConstantIdentifier = null, lpVarIdentifier = "x").isConstant(),
      "Term with variable defines should not be constant",
    )
    assertFalse(
      LPExpressionTerm(coefficient = null, lpConstantIdentifier = "c", lpVarIdentifier = "x").isConstant(),
      "Term with variable defines should not be constant",
    )
  }

  @Test
  @DisplayName("Test equality of LPExpressionTerm")
  fun testEquals() {
    var termToCheck = LPExpressionTerm(coefficient = null, lpConstantIdentifier = "c", lpVarIdentifier = "x")
    assertFalse(termToCheck.equals(null), "Comparison with a null expression should return false")
    assertFalse(termToCheck.equals("c"), "Comparison with a different object should return false")
    assertEquals(termToCheck, termToCheck, "Checks to the same reference should return true")
    assertNotEquals(
      termToCheck,
      LPExpressionTerm(coefficient = 1.0, lpConstantIdentifier = null, lpVarIdentifier = "x"),
      "Different coefficient type should not be equal",
    )
    assertNotEquals(
      termToCheck,
      LPExpressionTerm(coefficient = null, lpConstantIdentifier = "c", lpVarIdentifier = null),
      "Comparison between terms with the same coefficient but a different variable should not be equal",
    )
    assertNotEquals(
      termToCheck,
      LPExpressionTerm(coefficient = null, lpConstantIdentifier = "c1", lpVarIdentifier = "x"),
      "Comparison between coefficients of the same type should not be equal",
    )
    assertEquals(
      termToCheck,
      LPExpressionTerm(coefficient = null, lpConstantIdentifier = "c", lpVarIdentifier = "x"),
      "Comparison with a different object should return false",
    )

    // Test for equals and hashCode
    var equalTerm = LPExpressionTerm(coefficient = null, lpConstantIdentifier = "c", lpVarIdentifier = "x")
    var differentTerm = LPExpressionTerm(coefficient = 1.0, lpConstantIdentifier = null, lpVarIdentifier = "y")
    assertEquals(termToCheck.hashCode(), equalTerm.hashCode(), "Equal terms should have the same hashCode")
    assertEquals(termToCheck.toString(), equalTerm.toString(), "Equal terms should have the same toString()")
    assertNotEquals(
      termToCheck.hashCode(),
      differentTerm.hashCode(),
      "Different terms should not have the same hashCode",
    )
    assertNotEquals(
      termToCheck.toString(),
      differentTerm.toString(),
      "Different terms should not have the same toString()",
    )

    termToCheck = LPExpressionTerm(coefficient = null, lpConstantIdentifier = "c", lpVarIdentifier = null)
    equalTerm = LPExpressionTerm(coefficient = null, lpConstantIdentifier = "c", lpVarIdentifier = null)
    differentTerm = LPExpressionTerm(coefficient = 1.0, lpConstantIdentifier = null, lpVarIdentifier = "y")
    assertEquals(termToCheck.hashCode(), equalTerm.hashCode(), "Equal terms should have the same hashCode")
    assertEquals(termToCheck.toString(), equalTerm.toString(), "Equal terms should have the same toString()")
    assertNotEquals(
      termToCheck.hashCode(),
      differentTerm.hashCode(),
      "Different terms should not have the same hashCode",
    )
    assertNotEquals(
      termToCheck.toString(),
      differentTerm.toString(),
      "Different terms should not have the same toString()",
    )
  }
}