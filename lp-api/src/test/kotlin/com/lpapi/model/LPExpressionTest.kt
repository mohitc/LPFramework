package com.lpapi.model

import com.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPExpressionTest {
  private val log = KotlinLogging.logger("LPExpressionTest")

  class TestCaseDesc constructor(
    val case: String,
    val testCase: ((LPExpression) -> Unit),
    val invalidCase: ((LPExpression) -> Unit),
  )
  private val testCase = listOf(
    TestCaseDesc(
      "Fixed constant term",
      testCase = { t -> t.add(2) },
      invalidCase = { t -> t.add(3) }
    ),
    TestCaseDesc(
      "LPConstant term",
      testCase = { t -> t.add(LPConstant("c")) },
      invalidCase = { t -> t.add(LPConstant("not-c")) }
    ),
    TestCaseDesc(
      "LPConstant identifier",
      testCase = { t -> t.add("d") },
      invalidCase = { t -> t.add("again-not-d") }
    ),
    TestCaseDesc(
      "LPConstant identifier",
      testCase = { t -> t.add("d") },
      invalidCase = { t -> t.add("again-not-d") }
    ),
    TestCaseDesc(
      "LPVar",
      testCase = { t -> t.addTerm(LPVar("x", LPVarType.BOOLEAN)) },
      invalidCase = { t -> t.addTerm(LPVar("not-x", LPVarType.BOOLEAN)) }
    ),
    TestCaseDesc(
      "LPVarIdentifier",
      testCase = { t -> t.addTerm("y") },
      invalidCase = { t -> t.addTerm("not-y") }
    ),
    TestCaseDesc(
      "LPConstant and LPVar",
      testCase = { t -> t.addTerm(LPConstant("a"), LPVar("z", LPVarType.DOUBLE)) },
      invalidCase = { t -> t.addTerm(LPConstant("not-a"), LPVar("z", LPVarType.DOUBLE)) }
    ),
    TestCaseDesc(
      "LPConstantIdentifier and LPVarIdentifier",
      testCase = { t -> t.addTerm("b", "w") },
      invalidCase = { t -> t.addTerm("b", "not-w") }
    ),
    TestCaseDesc(
      "Constant and LPVar",
      testCase = { t -> t.addTerm(2, LPVar("p", LPVarType.INTEGER)) },
      invalidCase = { t -> t.addTerm(2, LPVar("not-p", LPVarType.INTEGER)) }
    ),
    TestCaseDesc(
      "Constant and LPVarIdentifier",
      testCase = { t -> t.addTerm(2, "q") },
      invalidCase = { t -> t.addTerm(3, "q") }
    ),
  )

  @Test
  @DisplayName("Test equality of LPExpression")
  fun testEquals() {
    val expr = LPExpression()

    // comparison with other object or nil values does not work
    assertFalse(expr.equals(null), "Equality with null value fails")
    assertFalse(expr.equals(LPObjective()), "Equality with different types")

    // Progressively run tests, adding different types of coefficients to the expression

    testCase.forEach {
      val copy = expr.copy()
      assertEquals(expr, copy, "Copies of expressions are equal")
      // Perform operations on the expressions and copies to check equality
      // Create copy and perform different operation on invalid copy
      val invalidCopy = expr.copy()
      it.invalidCase(invalidCopy)

      // Perform the same operations on the expression and copy
      it.testCase(expr)
      it.testCase(copy)
      assertEquals(expr, copy, "Expressions with same terms including ${it.case} are equal")
      assertEquals(
        expr.hashCode(), copy.hashCode(),
        "Expressions with same terms including ${it.case} have the same hashCode"
      )
      assertNotEquals(
        expr, invalidCopy,
        "Expressions with different terms include ${it.case} are not equal"
      )
    }

    // test that same instances are equal
    assertEquals(expr, expr, "Same references are equal")

    // test that equality is independent of sorting
    val newExpr = LPExpression()
    testCase.reversed().forEach { it.testCase(newExpr) }
    assertEquals(expr, newExpr, "Equality is independent of the order of terms")
    assertEquals(
      expr.hashCode(), newExpr.hashCode(),
      "Hashcode equality is independent of the order of terms"
    )

    // generate two expressions that split the terms into two expressions
    val expressionEven = LPExpression()
    val expressionOdd = LPExpression()
    var i = 0
    testCase.forEach {
      kotlin.run {
        when (i % 2) {
          0 -> it.testCase(expressionEven)
          1 -> it.testCase(expressionOdd)
        }
        i++
      }
    }
    assertEquals(expr, expressionEven.add(expressionOdd), "testing sum of expressions")

    val testExpression = LPExpression()
    testExpression.expression = expressionEven.add(expressionOdd).expression
    assertEquals(expr, testExpression, "testing setExpression")

    log.info { "Expression tested $expr" }
  }
}