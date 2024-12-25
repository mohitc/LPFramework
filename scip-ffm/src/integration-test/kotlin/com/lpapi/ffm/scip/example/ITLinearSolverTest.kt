package com.lpapi.ffm.scip.example

import com.lpapi.ffm.scip.*
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs


class ITLinearSolverTest {
  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  @Test
  fun testSolveFeasibleProblem() {
    val model = SCIPProblem()
    log.info { "Creating problem" }
    var retCode = model.createProblem("simple")
    assertEquals(retCode, SCIPRetCode.SCIP_OKAY, "createProblem() want OKAY got $retCode")

    retCode = model.includeDefaultPlugins()
    assertEquals(retCode, SCIPRetCode.SCIP_OKAY, "includeProblem() want OKAY got $retCode")

    log.info { "Creating variables" }
    val x = model.createVar("x", 2.0, 3.0, 1.0, SCIPVarType.SCIP_VARTYPE_CONTINUOUS)
    assertNotNull(x, "createVar(x, ..) want not null got null")
    val y = model.createVar("y", 0.0, model.infinity(), -3.0, SCIPVarType.SCIP_VARTYPE_INTEGER)
    assertNotNull(x, "createVar(y, ..) want not null got null")


    log.info { "Creating constraint" }
    val vars = listOfNotNull(x, y)
    val vals = listOf(1.0, 2.0)
    val constraint = model.createConstraint("linearConstraint", vars, vals, -model.infinity(), 10.0)
    assertNotNull(constraint, "createConstraint() want not null got null")
    log.info { "Releasing Constraint" }
    // release constraint (if not needed anymore)
    if (constraint != null) {
      retCode = model.releaseConstraint(constraint)
      assertEquals(retCode, SCIPRetCode.SCIP_OKAY, "releaseConstraint() want OKAY got $retCode")
    }

    log.info { "Initializing Model Parameters" }
    // set parameters
    retCode = model.setRealParam("limits/time", 100.0)
    assertEquals(retCode, SCIPRetCode.SCIP_OKAY, "Time limit could not be set successfully")
    model.setRealParam("limits/memory", 10000.0)
    assertEquals(retCode, SCIPRetCode.SCIP_OKAY, "Memory limit could not be set successfully")
    model.setLongintParam("limits/totalnodes", 1000)
    assertEquals(retCode, SCIPRetCode.SCIP_OKAY, "Total Node limit could not be set successfully")


    // solve problem
    log.info { "Solving the problem" }
    retCode = model.solve()
    assertEquals(retCode, SCIPRetCode.SCIP_OKAY, "Solve() want OKAY got $retCode")

    log.info { "Extracting Results" }
    val gap = model.getGap()
    assertTrue(abs(gap) <= 0.0001, "Gap() wan ~=0, got=$gap")

    val status = model.getStatus()
    assertEquals(status, SCIPStatus.OPTIMAL, "getStatus() want optimal got $status")

    val sol = model.getBestSol()
    assertNotNull(sol, "getBestSol() want not null got null")
    log.info { "Extracting value of variable x" }
    val xVal = model.getSolVal(sol!!, x!!)
    log.info { "X = $xVal" }
    assertTrue(abs( xVal - 2) <= 0.0001, "Solution Value: Want X~=2 got $xVal")
    val yVal = model.getSolVal(sol, y!!)
    log.info { "Y = $yVal" }
    assertTrue(abs( yVal - 4) <= 0.0001, "Solution Value: Want Y~=4 got $yVal")

    log.info { "Releasing variable" }
    retCode = model.releaseVar(x)
    assertEquals(retCode, SCIPRetCode.SCIP_OKAY, "releaseVar(x) want OKAY got $retCode")
    retCode = model.releaseVar(y)
    assertEquals(retCode, SCIPRetCode.SCIP_OKAY, "releaseVar(y) want OKAY got $retCode")

    log.info { "Releasing model" }
    retCode = model.freeProblem()
    assertEquals(retCode, SCIPRetCode.SCIP_OKAY, "freeProblem(y) want OKAY got $retCode")
  }
}