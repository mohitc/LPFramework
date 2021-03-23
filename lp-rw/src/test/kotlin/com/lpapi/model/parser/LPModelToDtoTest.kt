package com.lpapi.model.parser

import com.lpapi.model.*
import com.lpapi.model.dto.*
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPModelToDtoTest {

  private val log = KotlinLogging.logger(LPModelToDtoTest::javaClass.name)

  private fun varsToDto() = Stream.of(
      Arguments.of("Bounds for boolean variable default to 0,1",
          "test", LPVar("x", LPVarType.BOOLEAN),
          LPVarDto(identifier = "x", type = LPVarType.BOOLEAN, lbound = 0.0, ubound = 1.0)),
      Arguments.of("Explicitly defined bounds for boolean variables are respected",
          "some-other-group", LPVar("x", LPVarType.BOOLEAN, 0.0, 0.0),
          LPVarDto(identifier = "x", type = LPVarType.BOOLEAN, lbound = 0.0, ubound = 0.0)),
      Arguments.of("Boolean variables can also have bounds beyond 1",
          "another-group", LPVar("x", LPVarType.BOOLEAN, 1.0, 4.0),
          LPVarDto(identifier = "x", type = LPVarType.BOOLEAN, lbound = 1.0, ubound = 4.0)),
      Arguments.of("Integer variables with no bounds default to (0,0)",
          "group", LPVar("i", LPVarType.INTEGER),
          LPVarDto(identifier = "i", type = LPVarType.INTEGER, lbound = 0.0, ubound = 0.0)),
      Arguments.of("Integer variables with bounds set is converted correctly",
          "group", LPVar("i", LPVarType.INTEGER, 0.75, 2.5),
          LPVarDto(identifier = "i", type = LPVarType.INTEGER, lbound = 0.75, ubound = 2.5)),
      Arguments.of("Double variables with no bounds default to (0,0)",
          "group", LPVar("d", LPVarType.DOUBLE),
          LPVarDto(identifier = "d", type = LPVarType.DOUBLE, lbound = 0.0, ubound = 0.0)),
      Arguments.of("Double variables with bounds set is converted correctly",
          "group", LPVar("d", LPVarType.DOUBLE, -1.2, 3.3),
          LPVarDto(identifier = "d", type = LPVarType.DOUBLE, lbound = -1.2, ubound = 3.3)),
  )

  @ParameterizedTest(name="{0}")
  @MethodSource("varsToDto")
  fun testVarToDto(testCase: String, group: String, varToAdd: LPVar, expected: LPVarDto) {
    val model = LPModel("test")
    model.variables.add(group, varToAdd)
    val modelDto = LPModelParser().generateModelDto(model)
    log.info { "Generated Model DTO: $modelDto" }
    assertTrue(modelDto.vars.containsKey(group), "Model should have group identifier $group")
    assertTrue(modelDto.vars[group]!!.size == 1, "Model should only have one variable under group $group")
    assertTrue(modelDto.vars[group]!!.contains(expected),
        "DTO under group identifier $group do not match, want $expected found ${modelDto.vars[group]}")
  }

  // helper function to populate results for testing
  private fun populateResult(v : LPVar, result: Number) :LPVar {
    v.populateResult(result)
    return v
  }

  private fun varsToResultDto() = Stream.of(
      Arguments.of("Results for Boolean variables are rounded to the nearest Integer (round down)",
          "test", populateResult(LPVar("x", LPVarType.BOOLEAN), 0.45),
          LPVarResultDto(identifier = "x", result= 0)),
      Arguments.of("Results for Boolean variables are rounded to the nearest Integer (round up)",
          "test-up", populateResult(LPVar("x", LPVarType.BOOLEAN), 0.62),
          LPVarResultDto(identifier = "x", result= 1)),
      Arguments.of("Results for Integer variables are rounded to the nearest Integer (round down)",
          "test", populateResult(LPVar("i", LPVarType.INTEGER), 7.25),
          LPVarResultDto(identifier = "i", result= 7)),
      Arguments.of("Results for Integer variables are rounded to the nearest Integer (round up)",
          "test-up", populateResult(LPVar("i", LPVarType.INTEGER), 100.81),
          LPVarResultDto(identifier = "i", result= 101)),
  )

  @ParameterizedTest(name="{0}")
  @MethodSource("varsToResultDto")
  fun testVarToResultDto(testCase: String, group: String, varToAdd: LPVar, expected: LPVarResultDto) {
    val model = LPModel("test")
    model.variables.add(group, varToAdd)
    // Solution Status is set to facilitate DTO generation
    model.solution = LPModelResult(status = LPSolutionStatus.UNKNOWN,
        objective = 0.0,
        mipGap = null,
        computationTime = null)
    val modelResultDto = LPModelParser().generateModelResultDto(model)
    log.info { "Generated Model DTO: $modelResultDto" }
    assertTrue(modelResultDto.vars!!.size == 1, "Model should only have one variable $group")
    assertTrue(modelResultDto.vars!!.contains(expected),
        "DTO under group identifier $group do not match, want $expected found ${modelResultDto.vars}")
    val got = modelResultDto.vars!!.filter { it.identifier==varToAdd.identifier }.firstOrNull()
    assertEquals(got?.identifier, expected.identifier,
        "Identifiers of the found and the expected value should match")
    assertEquals(got?.result, expected.result,
        "Value of the found and the expected value should match")
  }

  private fun constantsToDto() = Stream.of(
      Arguments.of("Constant without supplied value defaults to 0", "test-group", LPConstant("c"),
          LPConstantDto("c", 0.0)),
      Arguments.of("Constant without supplied value is reflected in DTO", "test-group",
          LPConstant("c", 23), LPConstantDto("c", 23.0))
  )

  @ParameterizedTest(name="{0}")
  @MethodSource("constantsToDto")
  fun testConstantToDto(testCase: String, group: String, constantToAdd: LPConstant, expected: LPConstantDto) {
    val model = LPModel("test")
    model.constants.add(group, constantToAdd)
    val modelDto = LPModelParser().generateModelDto(model)
    log.info { "Generated Model DTO: $modelDto" }
    assertTrue(modelDto.constants.containsKey(group), "Model should have group identifier $group")
    assertTrue(modelDto.constants[group]!!.size == 1,
        "Model should only have one constant under group $group")
    assertTrue(modelDto.constants[group]!!.contains(expected),
        "DTO under group identifier $group do not match, want $expected found ${modelDto.constants[group]}")
  }

  private fun constraintsToDto() = Stream.of(
      Arguments.of(
          "Constraints with constants", fun () : LPConstraint {
        val c = LPConstraint("test")
        c.lhs.addTerm(2, "x").addTerm("y").addTerm(3.5, "z")
        c.rhs.addTerm(4, "q").add(3)
        c.operator = LPOperator.GREATER_EQUAL
        return c
      }, LPConstraintDto(identifier = "test", operator = LPOperator.GREATER_EQUAL,
          lhs = LPExpressionDto(terms = listOf(
              LPExpressionTermDto(coefficient = 2.0, varName = "x", constant = null),
              LPExpressionTermDto(coefficient = 1.0, varName = "y", constant = null),
              LPExpressionTermDto(coefficient = 3.5, varName = "z", constant = null),
          )),
          rhs = LPExpressionDto(terms = listOf(
              LPExpressionTermDto(coefficient = 4.0, varName = "q", constant = null),
              LPExpressionTermDto(coefficient = 3.0, varName = null, constant = null),
          )
          )
      )
      ),
      Arguments.of(
          "Constraints with constant identifiers", fun () : LPConstraint {
        val c = LPConstraint("test")
        c.lhs.addTerm("a", "x").addTerm("b","y").addTerm("c", "z")
        c.rhs.add("d")
        c.operator = LPOperator.EQUAL
        return c
      }, LPConstraintDto(identifier = "test", operator = LPOperator.EQUAL,
          lhs = LPExpressionDto(terms = listOf(
              LPExpressionTermDto(coefficient = null, varName = "x", constant = "a"),
              LPExpressionTermDto(coefficient = null, varName = "y", constant = "b"),
              LPExpressionTermDto(coefficient = null, varName = "z", constant = "c"),
          )),
          rhs = LPExpressionDto(terms = listOf(
              LPExpressionTermDto(coefficient = null, varName = null, constant = "d"),
          )
          )
      )
      ),
  )

  @ParameterizedTest(name="{0}")
  @MethodSource("constraintsToDto")
  fun testConstraintToDto(testCase: String, generator: ()->  LPConstraint,  expected: LPConstraintDto) {
    val model = LPModel("test")
    val group = "default-group"
    model.constraints.add(group, generator())
    val modelDto = LPModelParser().generateModelDto(model)
    log.info { "Generated Model DTO: $modelDto" }
    assertTrue(modelDto.constraints.containsKey(group), "Model should have group identifier : $group")
    assertTrue(modelDto.constraints[group]!!.size == 1,
        "Model should only have one constant under group : $group")
    assertTrue(modelDto.constraints[group]!!.contains(expected),
        "DTO under group identifier $group do not match, want $expected found ${modelDto.constraints[group]}")
  }

  private fun modelToResultDto() = Stream.of (
      Arguments.of (
          "Model with no solution set", fun (): LPModel {
        return LPModel("test")
      },
          LPModelResultDto(
              status = null,
              computed = false,
              objective = null,
              computationTime = null,
              mipGap = null,
              vars = null
          )
      ),
      Arguments.of (
          "Model with no variables", fun (): LPModel {
        val model = LPModel("test")
        model.solution = LPModelResult(solnStatus = LPSolutionStatus.UNKNOWN)
        return model
      },
          LPModelResultDto(
              status = LPSolutionStatus.UNKNOWN,
              computed = true,
              objective = null,
              computationTime = null,
              mipGap = null,
              vars = null
          )
      ),
      Arguments.of (
          "Model with all result parameters set", fun (): LPModel {
        val model = LPModel("test")
        model.solution = LPModelResult(
            status = LPSolutionStatus.OPTIMAL,
            objective = 2.0,
            mipGap = 0.1,
            computationTime = 32)
        return model
      },
          LPModelResultDto(
              status = LPSolutionStatus.OPTIMAL,
              computed = true,
              objective = 2.0,
              computationTime = 32,
              mipGap = 0.1,
              vars = listOf(),
          )
      )
  )

  @ParameterizedTest(name="{0}")
  @MethodSource("modelToResultDto")
  fun testModelToResultDto(testCase: String, generator: ()->  LPModel,  expected: LPModelResultDto) {
    val resultDto = LPModelParser().generateModelResultDto(generator())
    log.info { "Generated Model Result DTO: $resultDto" }
    assertEquals(resultDto, expected,
        "Expected Result DTO $expected got $resultDto")
  }

}