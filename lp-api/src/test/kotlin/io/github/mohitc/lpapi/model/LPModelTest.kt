package io.github.mohitc.lpapi.model

import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPModelTest {
  @Test
  @DisplayName("Test reduce for objective functions")
  fun testReduceObjective() {
    val objective = LPObjective()
    val model = LPModel()

    // empty objective is reduced successfully
    Assertions.assertNotNull(model.reduce(objective), "Empty objective can be reduced correctly")

    objective.expression.addTerm("x")
    Assertions.assertNull(model.reduce(objective), "Failed expression reduction should return null value")

    model.variables.add(LPVar("x", LPVarType.BOOLEAN))
    model.variables.add(LPVar("y", LPVarType.BOOLEAN))
    model.constants.add(LPConstant("c", 2))
    objective.expression
      .addTerm(2, "x")
      .add(3)
      .addTerm("c", "y")
      .addTerm(2, "y")
    val expectedObjective = LPObjective()
    expectedObjective.expression
      .add(3)
      .addTerm(3, "x")
      .addTerm(4, "y")

    LPObjectiveType.values().forEach {
      objective.objective = it
      expectedObjective.objective = it
      Assertions.assertEquals(
        model.reduce(objective),
        expectedObjective,
        "Reduced objective is set correctly in the model, and the objective type is maintained",
      )
      Assertions.assertNotEquals(
        model.reduce(objective),
        objective,
        "Reduction does not alter original objective",
      )
    }
  }

  @Test
  @DisplayName("Test reduce for expressions")
  fun testReduceExpression() {
    val model = LPModel()

    var invalidExpr =
      io.github.mohitc.lpapi.model
        .LPExpression()
    invalidExpr.expression.add(
      0,
      LPExpressionTerm(coefficient = null, lpConstantIdentifier = null, lpVarIdentifier = null),
    )
    Assertions.assertNull(
      model.reduce(invalidExpr),
      "Expressions with invalid terms (all values null) are not reduced",
    )
    // Assume variable is present
    model.variables.add(LPVar("invalid-var", LPVarType.BOOLEAN))
    invalidExpr =
      io.github.mohitc.lpapi.model
        .LPExpression()
    invalidExpr.expression.add(
      0,
      LPExpressionTerm(coefficient = null, lpConstantIdentifier = null, lpVarIdentifier = "invalid-var"),
    )
    Assertions.assertNull(model.reduce(invalidExpr), "Expressions with invalid coefficients are not reduced")

    val expr =
      io.github.mohitc.lpapi.model
        .LPExpression()
    val expectedExpression =
      io.github.mohitc.lpapi.model
        .LPExpression()

    Assertions.assertNotNull(model.reduce(expr), "Empty expression can be reduced")

    expr.addTerm("x")
    Assertions.assertNull(model.reduce(expr), "Expression with undefined variables is not reduced")

    model.variables.add(LPVar("x", LPVarType.BOOLEAN))
    Assertions.assertNotNull(
      model.reduce(expr),
      "Expressions where constant terms and vars are defined can be reduced",
    )
    expr.addTerm("x")
    expectedExpression.addTerm(2, "x")
    Assertions.assertEquals(
      model.reduce(expr),
      expectedExpression,
      "Reduction combines multiple terms with constant values into a single term",
    )

    expr.add(1)
    Assertions.assertNotNull(
      model.reduce(expr),
      "Expression with a numerical terms can be reduced",
    )

    expr.add("c")
    Assertions.assertNull(
      model.reduce(expr),
      "Expression with a constant terms that is not defined is not reduced",
    )
    model.constants.add(LPConstant("c", 1))

    Assertions.assertNotNull(model.reduce(expr), "Expressions where constant terms are defined can be reduced")

    expr.add(2).add("d")
    model.constants.add(LPConstant("d", 2))
    expectedExpression.add(6)
    Assertions.assertEquals(
      model.reduce(expr),
      expectedExpression,
      "Reduction combines the constant identifiers and the fixed value into a single number",
    )

    model.variables.add(LPVar("y", LPVarType.BOOLEAN))
    expr.addTerm("b", "y")
    Assertions.assertNull(
      model.reduce(expr),
      "Expression with an undefined constant identifier is not reduced",
    )
    model.constants.add(LPConstant("b", 2))
    Assertions.assertNotNull(
      model.reduce(expr),
      "Expression with all variables and constant identifiers defined is reduced",
    )
    expr.addTerm(3, "y")
    expectedExpression.addTerm(5, "y")
    Assertions.assertEquals(
      model.reduce(expr),
      expectedExpression,
      "Reduction combines multiple terms with a combination of constant parameters and fixed values",
    )
    Assertions.assertNotEquals(
      model.reduce(expr),
      expr,
      "Reduction does not alter original expression",
    )
  }

  @Test
  @DisplayName("Test reduce for constraints")
  fun testReduceConstraint() {
    val model = LPModel()
    val constraint = LPConstraint("constraint")
    val expectedConstraint = LPConstraint("constraint")

    Assertions.assertNull(model.reduce(constraint), "Constraint with Empty terms on both sides fails")
    constraint.lhs.add(-2)
    Assertions.assertNull(model.reduce(constraint), "Constraint with no variables fails")
    constraint.rhs.addTerm(-1.0, "x")
    Assertions.assertNull(model.reduce(constraint), "Constraint with variable not defined in the model fails")
    model.variables.add(LPVar("x", LPVarType.BOOLEAN))
    Assertions.assertNotNull(model.reduce(constraint), "Constraint with var and constant is reduced properly")

    constraint.lhs.add("d")
    Assertions.assertNull(model.reduce(constraint), "Constraint with undefined constant identifier fails")
    model.constants.add(LPConstant("d", 1))
    Assertions.assertNotNull(
      model.reduce(constraint),
      "Constraint with var and constant/constant Identifier is reduced properly",
    )

    constraint.lhs.addTerm("c", "x")
    Assertions.assertNull(
      model.reduce(constraint),
      "Constraint with undefined constant identifier for variable fails",
    )
    model.constants.add(LPConstant("c", 3))
    Assertions.assertNotNull(
      model.reduce(constraint),
      "Constraint with var and constant/constant Identifier is reduced properly",
    )

    model.variables.add(LPVar("y", LPVarType.BOOLEAN))
    constraint.rhs.addTerm(2, "y")
    constraint.lhs.addTerm(3, "y")

    expectedConstraint.lhs
      .addTerm(4, "x") // (c+1) x
      .addTerm("y") // (3-2) y
    expectedConstraint.rhs.add(1) // (2-d)

    LPOperator.values().forEach {
      constraint.operator = it
      expectedConstraint.operator = it
      Assertions.assertEquals(
        model.reduce(constraint),
        expectedConstraint,
        "Constraint is reduced correctly, and preserves the direction of change",
      )
      Assertions.assertNotEquals(
        model.reduce(constraint),
        constraint,
        "Reduced value is not a copy of the original constraint",
      )
    }
  }

  @Test
  @DisplayName("Test Model Equality")
  fun testEquality() {
    val modelId = "test-model"
    val x = LPVar("x", LPVarType.DOUBLE, 0, 2)
    val a = LPConstant("a", 2)
    val b = LPConstant("b", 3)
    val constraint = LPConstraint("default-constraint")
    constraint.lhs.addTerm("a", "x").add("b")
    constraint.rhs.add(3)

    val assertEquals = fun (
      a: LPModel,
      b: LPModel,
      message: String,
    ) {
      Assertions.assertEquals(a, b, "equals(): $message")
      Assertions.assertEquals(a.hashCode(), b.hashCode(), "hashCode(): $message")
    }
    val assertNotEquals = fun (
      a: LPModel,
      b: LPModel,
      message: String,
    ) {
      Assertions.assertNotEquals(a, b, "not equals(): $message")
      Assertions.assertNotEquals(a.hashCode(), b.hashCode(), "not equals hashCode(): $message")
    }

    val model = LPModel(modelId)

    assertEquals(model, model, "Same reference is evaluated as equal")
    Assertions.assertFalse(model.equals(x), "Equality across different types is evaluated as false")
    assertNotEquals(model, LPModel("some-other-identifier"), "Equality considers model identifier")
    assertEquals(model, LPModel(modelId), "Equality considers model identifier")

    model.variables.add(x)
    model.constants.add(a)
    model.constants.add(b)
    model.constraints.add(constraint)

    val testModel = LPModel(modelId)
    assertNotEquals(model, testModel, "Models with different parameters are not equal")
    testModel.constants.add(a)
    testModel.constants.add(b)
    assertNotEquals(model, testModel, "Models with different parameters (same constants) are not equal")

    testModel.variables.add(x)
    assertNotEquals(model, testModel, "Models with different parameters (same variables and constants) are not equal")

    testModel.constraints.add(constraint)
    assertEquals(model, testModel, "Models with same parameters are equal")

    model.objective.expression.addTerm(3, "x")
    testModel.objective.expression.add(2)

    assertNotEquals(model, testModel, "Models with different objectives are not equal")

    model.objective.expression.add(2)
    testModel.objective.expression.addTerm(3, "x")

    assertEquals(model, testModel, "Models with same objectives are equal")

    model.solution =
      LPModelResult(
        status = LPSolutionStatus.UNKNOWN,
        objective = null,
        computationTime = null,
        mipGap = null,
      )
    testModel.solution =
      LPModelResult(
        status = LPSolutionStatus.OPTIMAL,
        objective = 1.0,
        computationTime = 2,
        mipGap = null,
      )
    assertNotEquals(model, testModel, "Models with different solution status are not equal")

    model.solution =
      LPModelResult(
        status = LPSolutionStatus.OPTIMAL,
        objective = 1.0,
        computationTime = 2,
        mipGap = null,
      )
    assertEquals(model, testModel, "Models with same solution status are equal")
  }
}