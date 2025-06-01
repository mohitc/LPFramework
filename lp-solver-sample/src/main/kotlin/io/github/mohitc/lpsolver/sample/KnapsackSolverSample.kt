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

open class KnapsackSolverSample {
  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  // Classical knapsack problem formulated as
  // Maximize Sum ( Profit (i) * X(i) )
  // Subject to Sum (Weight (i) * X (i) <= Capacity

  // Initialize Constants for the problem
  private val profitWeightPair =
    listOf(
      Pair(5, 2),
      Pair(3, 8),
      Pair(2, 4),
      Pair(7, 2),
      Pair(4, 5),
    )

  private val capacity = 10

  private val expectedResult = listOf(1, 0, 0, 1, 1)

  private val expectedObjective = 16

  val model: LPModel =
    LPModel("knapsack").apply {
      this.objective.objective = LPObjectiveType.MAXIMIZE
      for (i in 1..profitWeightPair.size) {
        this.variables.add(LPVar("x-$i", LPVarType.BOOLEAN))
        this.constants.add(LPConstant("Profit-$i").value(profitWeightPair.get(i - 1).first.toDouble()))
        this.constants.add(LPConstant("Weight-$i").value(profitWeightPair.get(i - 1).second.toDouble()))
        this.objective.expression.addTerm("Profit-$i", "x-$i")
      }
      this.constants.add(LPConstant("Capacity").value(capacity.toDouble()))
      this.constraints.add(
        LPConstraint("Weight Limit").apply {
          for (i in 1..profitWeightPair.size) {
            this.lhs.addTerm("Weight-$i", "x-$i")
          }
          this.rhs.add("Capacity")
          this.operator = LPOperator.LESS_EQUAL
        },
      )
    }

  @Test
  fun validateModel() {
    log.info { "Validating knapsack model" }
    assertTrue(model.validate(), "Model Validation failed in the knapsack solver")
  }

  @Test
  fun testSolver() {
    val solver = Solver.create(model)
    val ok = solver.initialize()
    assertTrue(ok, "solver.initialize() want true got false")
    val status = solver.solve()
    assertEquals(LPSolutionStatus.OPTIMAL, status, "solver.solve() want OPTIMAL got $status")
    log.info { model.solution }
    assertNotNull(model.solution, "Model should be computed successfully.")
    for (i in 1..expectedResult.size) {
      assertEquals(
        expectedResult[i - 1],
        model.variables.get("x-$i")?.result,
        "x-$i want ${expectedResult[i - 1]} got ${model.variables.get("x-$i")?.result}",
      )
    }
    assertTrue(
      abs(model.solution?.objective!! - expectedObjective) <= 0.001,
      "Objective want $expectedObjective got ${model.solution?.objective!!}",
    )
  }
}