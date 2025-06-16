package io.github.mohitc.lpsolver.test

import io.github.mohitc.lpsolver.sample.KnapsackSolverSample
import io.github.mohitc.lpsolver.sample.PrimitiveSolverSample
import io.github.mohitc.lpsolver.sample.SudokuSolverSample
import io.github.mohitc.lpsolver.sample.TravellingSalesmanSolverSample
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class SolverTest {
  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  private val testInstances: List<SolverTestInstance> =
    listOf(
      PrimitiveSolverSample(),
      KnapsackSolverSample(),
      SudokuSolverSample(),
      TravellingSalesmanSolverSample(),
    )

  private fun testArgs() =
    testInstances
      .map {
        Arguments.of(
          it.name(),
          it,
        )
      }.toList()

  @ParameterizedTest(name = "{0}")
  @MethodSource("testArgs")
  fun testModels(
    name: String,
    ti: SolverTestInstance,
  ) {
    log.info { "Testing model $name" }
    assertTrue(ti.initModel(), "initModel() want true got false")
    log.info { "Starting Solver and Validations for $name" }
    ti.solveAndValidate()
  }
}