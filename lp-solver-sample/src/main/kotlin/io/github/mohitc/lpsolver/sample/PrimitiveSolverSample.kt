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
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

open class PrimitiveSolverSample {
  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  val modelGenerator: () -> LPModel = {
    val model = LPModel("Test Instance")
    // Initializing variables
    model.variables.add(LPVar("X", LPVarType.BOOLEAN))
    model.variables.add(LPVar("Y", LPVarType.BOOLEAN))
    model.variables.add(LPVar("Z", LPVarType.BOOLEAN))

    // Objective function => Maximize : X + Y + 2Z
    model.objective.expression
      .addTerm("X")
      .addTerm("Y")
      .addTerm(2, "Z")
    model.objective.objective = LPObjectiveType.MAXIMIZE

    // Add constants
    // a = 1, b = 2, c = 3
    model.constants.add(LPConstant("a", 1))
    model.constants.add(LPConstant("b", 2))
    model.constants.add(LPConstant("c", 3))

    // Add Constraints
    // Constraint 1 : aX + bY + cZ <= 4
    val constraint1 = model.constraints.add(LPConstraint("Constraint 1"))
    constraint1
      ?.lhs
      ?.addTerm("a", "X")
      ?.addTerm("b", "Y")
      ?.addTerm("c", "Z")
    constraint1?.rhs?.add(4)
    constraint1?.operator = LPOperator.LESS_EQUAL

    // Constraint 2 : X + Y >= 2
    val constraint2 = model.constraints.add(LPConstraint("Constraint 2"))
    constraint2
      ?.lhs
      ?.addTerm("X")
      ?.addTerm("Y")
    constraint2?.rhs?.add(2)
    constraint2?.operator = LPOperator.GREATER_EQUAL

    model
  }

  val model: LPModel = modelGenerator()

  @Test
  fun validateModel() {
    assertTrue(model.validate(), "Model Validation failed in the primitive solver")
  }

  /** Function to be implemented in the specific solvers. Function takes in a model, solves it, and provides a model
   * with the results set */
  open fun initAndSolveModel(model: LPModel): LPModel? {
    val solver = Solver.create(model)
    solver.initialize()
    val status = solver.solve()
    return if (status != LPSolutionStatus.UNKNOWN &&
      status != LPSolutionStatus.INFEASIBLE &&
      status != LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED
    ) {
      solver.model
    } else {
      null
    }
  }

  /**Test function that validates the results computed by the model*/
  @Test
  fun testSolver() {
    val model = initAndSolveModel(model)
    log.info { model?.solution }
    assertNotNull(model, "Model should be computed successfully.")
    assertEquals(model?.variables?.get("X")?.result, 1, "X should be = 1")
    assertEquals(model?.variables?.get("Y")?.result, 1, "Y should be = 1")
    assertEquals(model?.variables?.get("Z")?.result, 0, "Z should be = 0")
    assertEquals(model?.solution?.status, LPSolutionStatus.OPTIMAL, "Expect optimal result")
    assertTrue(abs(model?.solution?.objective!! - 2) < 0.001, "Objective value for optimal result should be 2.0")
  }
}