package io.github.mohitc.scip.ffm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ITSCIPProblemTest {
  @Test
  fun testCallAfterClose() {
    val model = SCIPProblem()
    model.close()
    // make sure that multiple calls to close result in no issue
    model.close()
    // Check that calls to methods post close result in runtime exceptions
    assertThrows<RuntimeException> {
      model.createProblem("test")
    }
  }

  @Test
  fun testCreateAndReleaseVar() {
    SCIPProblem().use { model ->
      model.createProblem("test")
      val x = model.createVar("x", 0.0, 1.0, 1.0, SCIPVarType.SCIP_VARTYPE_CONTINUOUS)
      assertNotNull(x)
      val retCode = model.releaseVar(x)
      assertEquals(SCIPRetCode.SCIP_OKAY, retCode)
    }
  }

  @Test
  fun testCreateAndReleaseConstraint() {
    SCIPProblem().use { model ->
      model.createProblem("test")
      assertEquals(SCIPRetCode.SCIP_OKAY, model.includeDefaultPlugins())
      val x = model.createVar("x", 0.0, 10.0, 1.0, SCIPVarType.SCIP_VARTYPE_CONTINUOUS)
      assertNotNull(x)
      val vars = listOfNotNull(x)
      val coeffs = listOf(1.0)
      val cons = model.createConstraint("c1", vars, coeffs, 0.0, 5.0)
      assertNotNull(cons)
      val retCode = model.releaseConstraint(cons)
      assertEquals(SCIPRetCode.SCIP_OKAY, retCode)
      assertEquals(SCIPRetCode.SCIP_OKAY, model.releaseVar(x))
    }
  }

  @Test
  fun testParams() {
    val stringParamName = "nlpi/ipopt/optfile"
    val charParamName = "branching/scorefunc"
    val realParamName = "branching/scorefac"
    val boolParamName = "branching/preferbinary"
    val intParamName = "conflict/minmaxvars"
    val longParamName = "iis/nodes"

    // Error cases
    SCIPProblem().use { model ->
      model.createProblem("test")
      model.includeDefaultPlugins()
      assertNull(model.getIntParam(stringParamName), "getIntParam wrong type")
      assertNull(model.getLongParam(stringParamName), "getLongParam wrong type")
      assertNull(model.getRealParam(stringParamName), "getRealParam wrong type")
      assertNull(model.getBoolParam(stringParamName), "getBoolParam wrong type")
      assertNull(model.getCharParam(stringParamName), "getCharParam wrong type")
      assertNull(model.getStringParam(intParamName), "getStringParam wrong type")
    }

    SCIPProblem().use { model ->
      model.createProblem("test")
      model.includeDefaultPlugins()
      assertEquals(SCIPRetCode.SCIP_OKAY, model.setStringParam(stringParamName, "-"))
      assertEquals("-", model.getStringParam(stringParamName))
      assertEquals(SCIPRetCode.SCIP_OKAY, model.setCharParam(charParamName, 's'))
      assertEquals('s', model.getCharParam(charParamName))
      assertEquals(SCIPRetCode.SCIP_OKAY, model.setRealParam(realParamName, 0.207))
      val gotVal = model.getRealParam(realParamName)
      assertNotNull(gotVal, "model.getRealVal() want non-null got null")
      assertTrue(Math.abs(0.207 - gotVal) < 0.0001)
      assertEquals(SCIPRetCode.SCIP_OKAY, model.setBoolParam(boolParamName, true))
      assertEquals(true, model.getBoolParam(boolParamName))
      assertEquals(SCIPRetCode.SCIP_OKAY, model.setIntParam(intParamName, 1))
      assertEquals(1, model.getIntParam(intParamName))
      assertEquals(SCIPRetCode.SCIP_OKAY, model.setLongParam(longParamName, 23L))
      assertEquals(23L, model.getLongParam(longParamName))
    }
  }

  @Test
  fun testObjSense() {
    SCIPProblem().use { model ->
      model.createProblem("test")
      model.includeDefaultPlugins()
      assertEquals(SCIPRetCode.SCIP_OKAY, model.maximize())
      assertEquals(SCIPObjSense.SCIP_OBJSENSE_MAXIMIZE, model.getObjSense())
      assertEquals(SCIPRetCode.SCIP_OKAY, model.minimize())
      assertEquals(SCIPObjSense.SCIP_OBJSENSE_MINIMIZE, model.getObjSense())
    }
  }

  @Test
  fun testWriteModel() {
    SCIPProblem().use { model ->
      model.createProblem("test")
      model.includeDefaultPlugins()
      val x = model.createVar("x", 0.0, 1.0, 1.0, SCIPVarType.SCIP_VARTYPE_CONTINUOUS)
      assertNotNull(x)
      val y = model.createVar("y", 0.0, 1.0, 1.0, SCIPVarType.SCIP_VARTYPE_CONTINUOUS)
      assertNotNull(y)
      model.releaseVar(x)
      model.releaseVar(y)
      val constr = model.createConstraint("x+y<=1", listOf(x, y), listOf(1.0, 1.0), 0.0, 1.0)
      assertNotNull(constr)
      model.releaseConstraint(constr)

      val outputFile =
        Paths
          .get(this.javaClass.getResource("/")!!.toURI())
          .resolve("output.nl")
          .toFile()

      val retCode = model.writeOriginalProblem(outputFile.absolutePath, "nl", SCIPBool.SCIP_TRUE)
      assertEquals(retCode, SCIPRetCode.SCIP_OKAY)
      assertTrue(outputFile.exists(), "Output file should exist")
    }
  }
}