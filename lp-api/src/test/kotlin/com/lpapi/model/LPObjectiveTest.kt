package com.lpapi.model

import com.lpapi.model.enums.LPObjectiveType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPObjectiveTest {
  private var log = KotlinLogging.logger("LPObjectiveTest")

  @Test
  @DisplayName("Test equality for LPObjective")
  fun testEquals() {
    // default constructor is initialized with objective maximize
    val expressionUnderTest: LPExpression = LPExpression().addTerm("x").add("2")
    val objectiveUnderTest = LPObjective(LPObjectiveType.MAXIMIZE, expressionUnderTest)

    log.info { "Objective function under test $objectiveUnderTest" }

    // identity results in true value
    assertEquals(objectiveUnderTest, objectiveUnderTest, "Test against the same reference results in true")
    assertFalse(objectiveUnderTest.equals(null), "Test against null results in false")
    assertFalse(objectiveUnderTest.equals(expressionUnderTest), "Test against different type results in false")

    var testObjective = LPObjective(LPObjectiveType.MAXIMIZE)
    testObjective.expression.add(expressionUnderTest)
    assertEquals(
      objectiveUnderTest,
      testObjective,
      "Different objectives with the same direction and expression result in true ",
    )
    assertEquals(
      objectiveUnderTest.hashCode(),
      testObjective.hashCode(),
      "Different objectives with the same direction and expression result in equal hash code",
    )

    testObjective = LPObjective(LPObjectiveType.MINIMIZE)
    testObjective.expression.add(expressionUnderTest)
    assertNotEquals(objectiveUnderTest, testObjective, "Different objective direction results in false")

    testObjective = LPObjective(LPObjectiveType.MAXIMIZE)
    testObjective.expression.add(expressionUnderTest).addTerm(2, "y")
    assertNotEquals(objectiveUnderTest, testObjective, "Different expressions results in false")
  }

  @Test
  @DisplayName("Test Constructors for the LPObjective")
  fun testConstructors() {
    // default constructor is initialized with objective maximize
    val expressionUnderTest = LPExpression().addTerm("x").add("2")
    val objectiveUnderTest = LPObjective(LPObjectiveType.MINIMIZE, expressionUnderTest)

    var testObjective = LPObjective()
    testObjective.expression.add(expressionUnderTest)
    assertNotEquals(objectiveUnderTest, testObjective, "Default optimization direction is MAXIMIZE")
    testObjective.objective = objectiveUnderTest.objective
    assertEquals(
      objectiveUnderTest,
      testObjective,
      "Default optimization direction is MAXIMIZE, and set changes the values correctly",
    )

    testObjective = LPObjective(LPObjectiveType.MINIMIZE)
    testObjective.expression = expressionUnderTest
    assertEquals(
      objectiveUnderTest,
      testObjective,
      "Explicitly setting objective in the constructor results in a match",
    )
  }
}