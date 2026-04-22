package io.github.mohitc.highs.ffm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ITHIGHSProblemTest {
  @Test
  fun testGetVarByName() {
    HIGHSProblem().use { h ->
      val gotColIndex = h.createVar("x", 0.0, 1.0, HIGHSVarType.CONTINUOUS)
      assertNotNull(gotColIndex, "createVar want not null got null")
      assertEquals(gotColIndex, h.getVarByName("x"), "getVarByName lookup")
      assertEquals(HIGHSVarType.CONTINUOUS, h.getVarType(gotColIndex!!))
      assertEquals("x", h.getVarName(gotColIndex), "getVarName")
    }
  }

  @Test
  fun testConstraints() {
    HIGHSProblem().use { h ->
      val x = h.createVar("x", 0.0, 10.0, HIGHSVarType.CONTINUOUS)
      assertNotNull(x, "x Initialization")
      val y = h.createVar("y", 0.0, 10.0, HIGHSVarType.CONTINUOUS)
      assertNotNull(y, "y Initialization")

      val constraintName = "c1"
      val rowIndex = h.createConstraint(constraintName, 0.0, 5.0, listOf(Pair(x!!, 1.0), Pair(y!!, 1.0)))
      assertNotNull(rowIndex)
      assertEquals(rowIndex, h.getConstraintByName(constraintName))
      assertEquals(constraintName, h.getConstraintName(rowIndex!!))
    }
  }

  @Test
  fun testObjective() {
    HIGHSObjective.entries.forEach { objective ->
      HIGHSProblem().use { h ->
        h.changeObjectiveDirection(objective)
        assertEquals(objective, h.getObjectiveDirection())

        h.changeObjectiveOffset(10.5)
        assertEquals(10.5, h.getObjectiveOffset())
      }
    }
  }

  @Test
  fun testSolveAndSolution() {
    HIGHSProblem().use { h ->
      // Maximize x + y s.t. x + y <= 1, x, y >= 0
      val x = h.createVar("x", 0.0, h.infinity(), HIGHSVarType.CONTINUOUS)
      val y = h.createVar("y", 0.0, h.infinity(), HIGHSVarType.CONTINUOUS)

      h.changeObjectiveCoefficient(x!!, 1.0)
      h.changeObjectiveCoefficient(y!!, 1.0)
      h.changeObjectiveDirection(HIGHSObjective.MAXIMIZE)

      h.createConstraint("c1", h.negInfinity(), 1.0, listOf(Pair(x, 1.0), Pair(y, 1.0)))

      val status = h.run()
      assertEquals(HIGHSStatus.OK, status)
      assertEquals(HIGHSModelStatus.OPTIMAL, h.getModelStatus())

      val solution = h.getSolution()
      assertNotNull(solution)
      val xBounds = solution!!.cols[x]
      val yBounds = solution.cols[y]
      assertNotNull(xBounds)
      assertNotNull(yBounds)
      assertEquals(1.0, xBounds!!.primal + yBounds!!.primal, 1e-6)
    }
  }

  @Test
  fun testBoolOption() {
    HIGHSProblem().use { h ->
      HIGHSBoolOption.entries.forEach { option ->
        h.setBoolOptionValue(option, option.default)
        assertEquals(option.default, h.getBoolOptionValue(option))
      }
    }
  }

  @Test
  fun testDoubleOption() {
    HIGHSProblem().use { h ->
      HIGHSDoubleOption.entries.forEach { option ->
        h.setDoubleOptionValue(option, option.default)
        assertEquals(option.default, h.getDoubleOptionValue(option))
      }
    }
  }

  @Test
  fun testIntOption() {
    HIGHSProblem().use { h ->
      HIGHSIntOption.entries.forEach { option ->
        h.setIntOptionValue(option, option.default)
        assertEquals(option.default, h.getIntOptionValue(option))
      }
    }
  }

  @Test
  fun testStringOption() {
    HIGHSProblem().use { h ->
      HIGHSStringOption.entries.forEach { option ->
        h.setStringOptionValue(option, option.default)
        assertEquals(option.default, h.getStringOptionValue(option))
      }
    }
  }
}