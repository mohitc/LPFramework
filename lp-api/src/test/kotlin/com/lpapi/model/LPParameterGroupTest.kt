package com.lpapi.model

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPParameterGroupTest {

  private val log = KotlinLogging.logger(LPParameterGroupTest::javaClass.name)

  class TestParameter(override val identifier: String) : LPParameter {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as TestParameter

      if (identifier != other.identifier) return false

      return true
    }

    override fun hashCode(): Int {
      return identifier.hashCode()
    }
  }

  @Test
  @DisplayName("Test Parameter groups in the LP Model")
  fun testParameterGroups() {
    val defaultGroupIdentifier = "Default Identifier"

    val pg = LPParameterGroup<TestParameter>(defaultGroupIdentifier)

    // at the point no parameters are added, the maps should be empty
    Assertions.assertEquals(pg.getAllGroups(), emptySet<String>(),
        "getAllGroups() is empty when no parameters are included")
    Assertions.assertEquals(pg.allValues().toSet(), emptySet<String>(),
        "allValues() is empty when no parameters are included")
    Assertions.assertNull(pg.getAllIdentifiers(defaultGroupIdentifier),
    "getAllIdentifiers() is null when no parameters are initialized")

    // add parameter
    log.info("Adding parameter to default group")
    var param = pg.add(TestParameter("x"))
    Assertions.assertNull(pg.add(TestParameter("x")),
        "adding a new parameters with the same identifier to the same group returns null")
    Assertions.assertNull(pg.add("some-other-group", TestParameter("x")),
        "adding a new parameters with the same identifier to a different group returns null")

    Assertions.assertNotNull(param, "adding a new parameter without existing parameter succeeds")
    Assertions.assertEquals(pg.getAllGroups(), setOf(defaultGroupIdentifier),
        "getAllGroups() should contain $defaultGroupIdentifier")
    Assertions.assertEquals(pg.getAllIdentifiers(defaultGroupIdentifier), setOf("x"),
        "getAllIdentifiers($defaultGroupIdentifier) should contain x")
    Assertions.assertEquals(pg.allValues().toSet(), setOf(TestParameter("x")),
        "allValues() should contain x")

    // Add a parameter to a different group
    val anotherGroup = "some-other-group"
    param = pg.add(anotherGroup, TestParameter("y"))
    Assertions.assertNotNull(param,
        "adding a new parameter without existing parameter succeeds in a non-empty group succeeds")
    Assertions.assertEquals(pg.getAllGroups(), setOf(defaultGroupIdentifier, anotherGroup),
        "getAllGroups() should contain $defaultGroupIdentifier, $anotherGroup")
    Assertions.assertEquals(pg.getAllIdentifiers(defaultGroupIdentifier), setOf("x"),
        "getAllIdentifiers($defaultGroupIdentifier) should contain x")
    Assertions.assertEquals(pg.getAllIdentifiers(anotherGroup), setOf("y"),
        "getAllIdentifiers($anotherGroup) should contain y")
    Assertions.assertEquals(pg.allValues().toSet(), setOf("x", "y").map { TestParameter(it) }.toSet(),
        "allValues() should contain x and y")

  }
}