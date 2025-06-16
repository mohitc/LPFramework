package io.github.mohitc.lpsolver.sample

import io.github.mohitc.lpapi.model.LPConstant
import io.github.mohitc.lpapi.model.LPConstraint
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPVar
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import io.github.mohitc.lpsolver.spi.Solver
import io.github.mohitc.lpsolver.test.SolverTestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.math.abs

class PrimitiveSolverSample : SolverTestInstance {
  var model: LPModel? = null

  override fun name(): String = "Primitive Model"

  override fun initModel(): Boolean {
    val localModel =
      LPModel("Test Instance").apply {
        // Initializing variables
        variables.add(LPVar("X", LPVarType.BOOLEAN))
        variables.add(LPVar("Y", LPVarType.BOOLEAN))
        variables.add(LPVar("Z", LPVarType.BOOLEAN))

        // Objective function => Maximize : X + Y + 2Z
        objective.expression
          .addTerm("X")
          .addTerm("Y")
          .addTerm(2, "Z")
        objective.objective = LPObjectiveType.MAXIMIZE

        // Add constants
        // a = 1, b = 2, c = 3
        constants.add(LPConstant("a", 1))
        constants.add(LPConstant("b", 2))
        constants.add(LPConstant("c", 3))

        // Add Constraints
        // Constraint 1 : aX + bY + cZ <= 4
        val constraint1 = constraints.add(LPConstraint("Constraint 1"))
        constraint1
          ?.lhs
          ?.addTerm("a", "X")
          ?.addTerm("b", "Y")
          ?.addTerm("c", "Z")
        constraint1?.rhs?.add(4)
        constraint1?.operator = LPOperator.LESS_EQUAL

        // Constraint 2 : X + Y >= 2
        val constraint2 = constraints.add(LPConstraint("Constraint 2"))
        constraint2
          ?.lhs
          ?.addTerm("X")
          ?.addTerm("Y")
        constraint2?.rhs?.add(2)
        constraint2?.operator = LPOperator.GREATER_EQUAL
      }
    model = localModel
    return localModel.validate()
  }

  /** Function to be implemented in the specific solvers. Function takes in a model, solves it, and provides a model
   * with the results set */
  override fun solveAndValidate() {
    val solver = Solver.create(model!!)
    solver.initialize()
    val status = solver.solve()
    assertEquals(status, LPSolutionStatus.OPTIMAL, "Expect optimal result")
    assertEquals(model?.variables?.get("X")?.result, 1, "X should be = 1")
    assertEquals(model?.variables?.get("Y")?.result, 1, "Y should be = 1")
    assertEquals(model?.variables?.get("Z")?.result, 0, "Z should be = 0")
    assertTrue(abs(model?.solution?.objective!! - 2) < 0.001, "Objective value for optimal result should be 2.0")
  }
}