package com.lpapi.ffm.highs.example

import com.lpapi.ffm.highs.Bounds
import com.lpapi.ffm.highs.HIGHSModelStatus
import com.lpapi.ffm.highs.HIGHSObjective
import com.lpapi.ffm.highs.HIGHSProblem
import com.lpapi.ffm.highs.HIGHSStatus
import com.lpapi.ffm.highs.HIGHSVarType
import com.lpapi.ffm.highs.Result
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ITLinearSolverTest {
  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  private class VariableParameters(
    val name: String,
    val lb: Double,
    val ub: Double,
    val varType: HIGHSVarType,
  )

  private class ConstraintParameters(
    val name: String,
    val varCoefficients: Map<String, Double>,
    val lb: Double,
    val ub: Double,
  )

  @Test
  fun testChangeObjectiveDirection() {
    val highsProblem = HIGHSProblem()

    HIGHSObjective.values().forEach { v ->
      val gotStatus = highsProblem.changeObjectiveDirection(v)
      assertEquals(HIGHSStatus.OK, gotStatus, "changeObjectiveDirection($v) want OK got $gotStatus")
      val gotObjective = highsProblem.getObjectiveDirection()
      assertEquals(v, gotObjective, "gotObjectiveDirection() want $v got $gotObjective")
    }
    highsProblem.cleanup()
  }

  @Test
  fun testChangeObjectiveOffset() {
    val highsProblem = HIGHSProblem()

    val wantOffset = 4.3
    val gotStatus = highsProblem.changeObjectiveOffset(wantOffset)
    assertEquals(HIGHSStatus.OK, gotStatus, "changeObjectiveOffset($wantOffset) want OK got $gotStatus")

    val gotOffset = highsProblem.getObjectiveOffset()
    assertTrue(Math.abs(wantOffset - gotOffset!!) < 0.001, "getObjectOffset() want $wantOffset got $gotOffset")

    highsProblem.cleanup()
  }

  @Test
  fun testCreateVars() {
    val highsProblem = HIGHSProblem()
    log.info { "Highs Problem initialized" }
    val varsToCreate =
      listOf(
        VariableParameters("a", 2.0, 3.0, HIGHSVarType.CONTINUOUS),
        VariableParameters("b", -10.0, 23.0, HIGHSVarType.INTEGER),
        VariableParameters("c", 3.4, 3.8, HIGHSVarType.SEMI_CONTINUOUS),
        VariableParameters("d", -22.0, -3.0, HIGHSVarType.SEMI_INTEGER),
        VariableParameters("e", 2.0, 35.0, HIGHSVarType.IMPLICIT_INTEGER),
      )
    val varIndex = mutableMapOf<String, Int>()
    varsToCreate.forEach { v ->
      val index = highsProblem.createVar(v.name, v.lb, v.ub, v.varType)
      assertNotNull(index, "createVar(${v.name}, ${v.lb}, ${v.ub}, ${v.varType}) want not null got null")
      varIndex[v.name] = index!!
    }

    varIndex.forEach { e ->
      val gotVarByName = highsProblem.getVarByName(e.key)
      assertEquals(gotVarByName, e.value, "getVarByName(${e.key}) want ${e.value} got $gotVarByName")

      val gotVarName = highsProblem.getVarName(e.value)
      assertEquals(gotVarName, e.key, "getVarName(${e.value}) want ${e.key} got $gotVarName")
    }

    highsProblem.cleanup()
  }

  @Test
  fun testCreateConstraints() {
    val highsProblem = HIGHSProblem()
    log.info { "Highs Problem initialized" }
    val varsToCreate =
      listOf(
        VariableParameters("a", 2.0, 3.0, HIGHSVarType.CONTINUOUS),
        VariableParameters("b", -10.0, 23.0, HIGHSVarType.INTEGER),
        VariableParameters("c", 3.4, 3.8, HIGHSVarType.CONTINUOUS),
        VariableParameters("d", -22.0, -3.0, HIGHSVarType.INTEGER),
      )
    val varIndex = mutableMapOf<String, Int>()
    varsToCreate.forEach { v ->
      val index = highsProblem.createVar(v.name, v.lb, v.ub, v.varType)
      assertNotNull(index, "createVar(${v.name}, ${v.lb}, ${v.ub}, ${v.varType}) want not null got null")
      varIndex[v.name] = index!!
    }
    val constraintsToCreate =
      listOf(
        ConstraintParameters(
          name = "a+b+c+d=1",
          varCoefficients =
            mapOf(
              Pair("a", 1.0),
              Pair("b", 1.0),
              Pair("c", 1.0),
              Pair("d", 1.0),
            ),
          lb = 1.0,
          ub = 1.0,
        ),
        ConstraintParameters(
          name = "2a-3b>2.5",
          varCoefficients =
            mapOf(
              Pair("a", 2.0),
              Pair("b", -3.0),
            ),
          lb = 2.5,
          ub = highsProblem.infinity(),
        ),
        ConstraintParameters(
          name = "3c-4d<12",
          varCoefficients =
            mapOf(
              Pair("c", 3.0),
              Pair("d", -4.0),
            ),
          lb = highsProblem.negInfinity(),
          ub = 12.0,
        ),
        ConstraintParameters(
          name = "-10<a-2b+3c-4d<10",
          varCoefficients =
            mapOf(
              Pair("a", 1.0),
              Pair("b", -2.0),
              Pair("c", 3.0),
              Pair("d", -4.0),
            ),
          lb = -10.0,
          ub = 10.0,
        ),
      )
    val constraintIndex = mutableMapOf<String, Int>()
    constraintsToCreate.forEach { c ->
      val index =
        highsProblem.createConstraint(
          c.name,
          c.lb,
          c.ub,
          c.varCoefficients.map { e -> Pair(varIndex[e.key]!!, e.value) },
        )
      assertNotNull(index, "createConstraint($c) want not null, got null")
      constraintIndex[c.name] = index!!
    }

    constraintIndex.forEach { c ->
      val gotConstraintByName = highsProblem.getConstraintByName(c.key)
      assertEquals(
        gotConstraintByName,
        c.value,
        "getConstraintByName(${c.key}) want ${c.value} got $gotConstraintByName",
      )

      val gotConstraintName = highsProblem.getConstraintName(c.value)
      assertEquals(gotConstraintName, c.key, "getConstraintName(${c.value}) want ${c.key} got $gotConstraintName")
    }

    highsProblem.cleanup()
  }

  @Test
  fun testSolveFeasibleProblem() {
    val highsProblem = HIGHSProblem()
    log.info { "Highs Problem initialized" }

    log.info { "Creating variables" }
    val varsToCreate =
      listOf(
        VariableParameters("x", 2.0, 3.0, HIGHSVarType.CONTINUOUS),
        VariableParameters("y", 0.0, 10.0, HIGHSVarType.INTEGER),
      )
    val varIndex = mutableMapOf<String, Int>()
    varsToCreate.forEach { v ->
      val index = highsProblem.createVar(v.name, v.lb, v.ub, v.varType)
      assertNotNull(index, "createVar(${v.name}, ${v.lb}, ${v.ub}, ${v.varType}) want not null got null")
      varIndex[v.name] = index!!
    }

    val objectiveCoefficients =
      mapOf(
        Pair("x", 1.0),
        Pair("y", -3.0),
      )
    objectiveCoefficients.forEach { e ->
      val gotStatus = highsProblem.changeObjectiveCoefficient(varIndex[e.key]!!, e.value)
      assertEquals(HIGHSStatus.OK, gotStatus, "changeObjectiveCoefficient(${e.key}, ${e.value}) want OK got $gotStatus")
    }

    val constraintsToCreate =
      listOf(
        ConstraintParameters(
          name = "x+2y<=10",
          varCoefficients =
            mapOf(
              Pair("x", 1.0),
              Pair("y", 2.0),
            ),
          lb = highsProblem.negInfinity(),
          ub = 10.0,
        ),
      )
    val constraintIndex = mutableMapOf<String, Int>()
    constraintsToCreate.forEach { c ->
      val index =
        highsProblem.createConstraint(
          c.name,
          c.lb,
          c.ub,
          c.varCoefficients.map { e -> Pair(varIndex[e.key]!!, e.value) },
        )
      assertNotNull(index, "createConstraint($c) want not null, got null")
      constraintIndex[c.name] = index!!
    }

    var retCode = highsProblem.changeObjectiveDirection(HIGHSObjective.MAXIMIZE)
    assertEquals(HIGHSStatus.OK, retCode, "changeObjectiveDirection(MAXIMIZE) want OK got $retCode")

    retCode = highsProblem.writeModel("model.lp")
    assertEquals(retCode, HIGHSStatus.OK, "writeModel() want OK got $retCode")

    retCode = highsProblem.run()
    assertEquals(HIGHSStatus.OK, retCode, "run() want OK got $retCode")

    log.info { "Model output" }
    highsProblem.writeSolution("model_results.soln")

    val status = highsProblem.getModelStatus()
    assertEquals(HIGHSModelStatus.OPTIMAL, status, "getModelStatus() want OPTIMAL got $status")

    val gotResult = highsProblem.getSolution()
    val wantResult =
      Result(
        cols =
          mapOf(
            Pair(0, Bounds(primal = 3.0, dual = 0.0)),
            Pair(1, Bounds(primal = 0.0, dual = 0.0)),
          ),
        rows =
          mapOf(
            Pair(0, Bounds(primal = 3.0, dual = 0.0)),
          ),
      )
    assertEquals(wantResult, gotResult, "getSolution() want $wantResult got $gotResult")

    log.info { "Cleaning up Highs Problem" }
    highsProblem.cleanup()
  }
}