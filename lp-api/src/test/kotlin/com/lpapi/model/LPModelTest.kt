package com.lpapi.model

import com.lpapi.model.enums.LPVarType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPModelTest {

  @Test
  @DisplayName("Validating constraint groups")
  fun lpModelValidationTest() {
    val lpModel = LPModel("testModel")
    Assertions.assertNotNull(
      lpModel.constants.add("group1", LPConstant("constant1", 1.0)),
      "Constant with unique identifier should be added in model"
    )
    Assertions.assertNotNull(
      lpModel.constants.add("group2", LPConstant("constant2", 1.0)),
      "Constant with unique identifier should be added in model"
    )

    Assertions.assertEquals(
      lpModel.constants.grouping.keys.size, 2,
      "Constant should contain 2 groups"
    )

    Assertions.assertNull(
      lpModel.constants.add(LPConstant("constant1", 1.0)),
      "Constant with same identifier but different group should not be supported"
    )
    Assertions.assertNotNull(
      lpModel.constants.add(LPConstant("constant3", 1.0)),
      "Constant with unique identifier should be added in model"
    )

    Assertions.assertEquals(
      lpModel.constants.grouping.keys.size, 3,
      "Constant with no group supplied should be added to default group"
    )
    Assertions.assertNotNull(
      lpModel.constants.grouping[LPModel.DEFAULT_CONSTANT_GROUP],
      "Default group should be created for constants if no group identifier is supplied"
    )
    lpModel.constants.grouping[LPModel.DEFAULT_CONSTANT_GROUP]?.contains("constant3")?.let {
      Assertions.assertTrue(
        it,
        "Constant with no group supplied should be added to default group"
      )
    }
    lpModel.validate()
  }

  @Test
  @DisplayName("Validating variables in the model")
  fun testVariables() {
    var lpModel = LPModel("testModel")
    lpModel.variables.add(LPVar("a", LPVarType.BOOLEAN, -2.0, -1.0))
    Assertions.assertFalse(lpModel.validate(), "Boolean variable with invalid bounds should fail validation")
    lpModel = LPModel("testModel")
    lpModel.variables.add(LPVar("a", LPVarType.BOOLEAN, 1.1, 3.0))
    Assertions.assertFalse(lpModel.validate(), "Boolean variable with invalid bounds should fail validation")
    lpModel = LPModel("testModel")
    lpModel.variables.add(LPVar("a", LPVarType.DOUBLE, 1.1, 1.0))
    Assertions.assertFalse(lpModel.validate(), "Variable with lower bound greater than the upper bound should" +
        " fail validation")

    lpModel = LPModel("testModel")
    lpModel.variables.add(LPVar("a", LPVarType.INTEGER, 2.1, 2.2))
    Assertions.assertFalse(lpModel.validate(), "Integer variable should have a value that is covered in ")

    lpModel = LPModel("testModel")
    lpModel.variables.add(LPVar("a", LPVarType.INTEGER, 1.9, 2.0))
    Assertions.assertTrue(lpModel.validate(), "Integer bounding should pass if variables have at least one " +
        "integer value between their bounds")
  }

  @Test
  @DisplayName("Validating constraints in the model")
  fun testConstraints() {
    var lpModel = LPModel("testModel")
    lpModel.constraints.add(LPConstraint("emptyConstraint"))
    Assertions.assertFalse(lpModel.validate(), "Constraint with empty LHS or RHS should fail validation")

    lpModel = LPModel("testModel")
    lpModel.constraints.add(LPConstraint("emptyConstraint"))?.lhs?.add(1.0)
    Assertions.assertFalse(lpModel.validate(), "Constraint with empty LHS or RHS should fail validation")

    lpModel = LPModel("testModel")
    lpModel.constraints.add(LPConstraint("emptyConstraint"))?.rhs?.add(1.0)
    Assertions.assertFalse(lpModel.validate(), "Constraint with empty LHS or RHS should fail validation")

    lpModel = LPModel("testModel")
    var constraint: LPConstraint? = lpModel.constraints.add(LPConstraint("nonEmptyConstraint"))
    constraint?.lhs?.add(1.0)
    constraint?.rhs?.add(1.0)
    Assertions.assertFalse(lpModel.validate(), "Constraint with non-empty LHS and RHS but with no variables " +
        "should fail validation")

    lpModel = LPModel("testModel")
    constraint = lpModel.constraints.add(LPConstraint("nonEmptyConstraint"))
    lpModel.variables.add(LPVar("x", LPVarType.BOOLEAN))
    constraint?.lhs?.add(1.0)?.addTerm(2.0, "x")
    constraint?.rhs?.add(1.0)
    Assertions.assertTrue(lpModel.validate(), "Constraint with non-empty LHS and RHS and with defined " +
        "variable should pass validation")

    // Constraint with both constant and identifier defined should fail validation
    lpModel = LPModel("testModel")
    lpModel.constants.add(LPConstant("c", 1.1))
    constraint = lpModel.constraints.add(LPConstraint("nonEmptyConstraint"))
    constraint?.lhs?.expression?.add(LPExpressionTerm(1.0, null, "c"))
    constraint?.rhs?.add(1.0)
    Assertions.assertFalse(lpModel.validate(), "Constraint with both coefficient and identifier defined " +
        "should fail validation")

    // Conditional checks on expressions to check if all terms are defined
    lpModel = LPModel("testModel")
    constraint = lpModel.constraints.add(LPConstraint("nonEmptyConstraint"))
    constraint?.lhs?.addTerm("a", "x")?.addTerm("b", "y")
    constraint?.rhs?.add(1.0)
    Assertions.assertFalse(lpModel.validate(), "Constraint with no variables and constants defined should " +
        "fail validation")
    lpModel.variables.add(LPVar("x", LPVarType.BOOLEAN))
    lpModel.variables.add(LPVar("y", LPVarType.BOOLEAN))
    Assertions.assertFalse(lpModel.validate(), "Constraint with no constants defined should fail validation")
    lpModel.constants.add(LPConstant("a", 1.1))
    lpModel.constants.add(LPConstant("b", 1.1))
    Assertions.assertTrue(lpModel.validate(), "Constraint with all variables and constants defined should " +
        "pass validation")
    val constant = LPConstant("c", 1.0)
    constraint?.rhs?.add(constant)
    Assertions.assertFalse(lpModel.validate(), "Constraint with no constants defined should fail validation")
    lpModel.constants.add(constant)
    Assertions.assertTrue(lpModel.validate(), "Constraint with all variables and constants defined should " +
        "pass validation")

    // Conditional checks on expressions to check if all terms are defined when expressions use constants
    constraint = lpModel.constraints.add(LPConstraint("nonEmptyConstraint2"))
    constraint?.lhs?.addTerm(1.0, "x2")?.addTerm(2.0, "y2")
    constraint?.rhs?.add(1.0)
    Assertions.assertFalse(lpModel.validate(), "Constraint with no variables defined should fail validation")
    lpModel.variables.add(LPVar("x2", LPVarType.BOOLEAN))
    lpModel.variables.add(LPVar("y2", LPVarType.BOOLEAN))
    Assertions.assertTrue(lpModel.validate(), "Constraint with all variables and constants defined should " +
        "pass validation")
  }
}