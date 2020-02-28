package com.lpapi.solver.sample

import com.lpapi.model.LPConstant
import com.lpapi.model.LPConstraint
import com.lpapi.model.LPModel
import com.lpapi.model.LPVar
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPVarType
import mu.KotlinLogging

abstract class PrimitiveSolverSample {

  val log = KotlinLogging.logger(this.javaClass.simpleName)

  fun initLpModel() : LPModel {
    val model = LPModel("Test Instance")
    //Initializing variables
    model.variables.add(LPVar("X", LPVarType.BOOLEAN))
    model.variables.add(LPVar("Y", LPVarType.BOOLEAN))
    model.variables.add(LPVar("Z", LPVarType.BOOLEAN))

    //Objective function => Maximize : X + Y + 2Z
    model.objective.expression
        .addTerm( "X")
        .addTerm("Y")
        .addTerm(2, "Z")
    model.objective.objective = LPObjectiveType.MAXIMIZE

    //Add constants
    //a = 1, b = 2, c = 3
    model.constants.add(LPConstant("a", 1))
    model.constants.add(LPConstant("b", 2))
    model.constants.add(LPConstant("c", 3))

    //Add Constraints
    //Constraint 1 : aX + bY + cZ <= 4
    val constraint1 = model.constraints.add(LPConstraint("Constraint 1"))
    constraint1?.lhs
        ?.addTerm("a", "X")
        ?.addTerm("b", "Y")
        ?.addTerm("c", "Z")
    constraint1?.rhs?.add(4)
    constraint1?.operator = LPOperator.LESS_EQUAL

    //Constraint 2 : X + Y >= 2
    val constraint2 = model.constraints.add(LPConstraint("Constraint 2"))
    constraint2?.lhs
        ?.addTerm("X")
        ?.addTerm("Y")
    constraint2?.rhs?.add(2)
    constraint2?.operator = LPOperator.GREATER_EQUAL

    return model
  }

  /** Function to be implemented in the specific solvers. Function takes in a model, solves it, and provides a model with
   * the results set */
  abstract fun initAndSolveModel(model: LPModel) : LPModel?

}