package com.lpapi.model

import com.lpapi.model.enums.LPVarType
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

  private fun assertNotEquals(a : LPParameterGroup<*>, b : LPParameterGroup<*>, condition:String) {
    Assertions.assertNotEquals(a, b, "Not Equals(): $condition")
    Assertions.assertNotEquals(a.hashCode(), b.hashCode(), "Not equal hashCode(): $condition")
  }

  private fun<T:LPParameter> assertEquals(a : LPParameterGroup<T>, b : LPParameterGroup<T>, condition:String) {
    Assertions.assertEquals(a, b, "Equals(): $condition")
    Assertions.assertEquals(a.hashCode(), b.hashCode(), "hashCode(): $condition")
  }

  @Test
  @DisplayName("Test Equality of Parameter groups")
  fun testEquality() {
    val defaultGroupIdentifier = "Default Identifier"
    val someOtherGroupIdentifier = "some-other-identifier"

    val pg = LPParameterGroup<TestParameter>(defaultGroupIdentifier)

    Assertions.assertNotEquals(pg, TestParameter("c"), "Comparison of different class type fails")

    assertEquals(pg, pg, "Reference to the same parameter group are equal")

    assertEquals(pg, LPParameterGroup<TestParameter>(someOtherGroupIdentifier),
        "Default identifiers with no parameters are equal")

    pg.add(TestParameter("x"))
    assertNotEquals(pg, LPParameterGroup<TestParameter>(someOtherGroupIdentifier),
        "Different parameters results in non-equality")
    val x = LPParameterGroup<LPVar>(defaultGroupIdentifier)
    x.add(LPVar("x", LPVarType.BOOLEAN))
    assertNotEquals(pg, x, "Same parameter identifiers and grouping, but different type are not equal")

    var b = LPParameterGroup<TestParameter>(defaultGroupIdentifier)
    b.add( TestParameter("y"))
    assertNotEquals(pg, b,"Different parameter in the same groups are not equal")

    b = LPParameterGroup<TestParameter>(defaultGroupIdentifier)
    b.add("some-other-group", TestParameter("x"))
    assertNotEquals(pg, b,"Same parameter in different groups are not equal")

    b = LPParameterGroup<TestParameter>(defaultGroupIdentifier)
    pg.add("some-other-group", TestParameter("y"))
    b.add("some-other-group", TestParameter("y"))
    b.add(TestParameter("x"))
    pg.add(TestParameter("z"))
    b.add(TestParameter("z"))
    assertEquals(pg, b,"Same parameter and group configuration results in equality")

  }
}