package com.lpapi.model.parser

import com.lpapi.model.LPModel
import com.lpapi.model.LPModelResult
import com.lpapi.model.dto.LPModelResultDto
import com.lpapi.model.dto.LPVarResultDto
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.solver.sample.PrimitiveSolverSample
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LPModelParserTest {

  private val log = KotlinLogging.logger("LPModelParserTest")

  private val sampleModel = fun (withResult: Boolean): LPModel {
    val model = object : PrimitiveSolverSample() {
      override fun initAndSolveModel(model: LPModel): LPModel? = null
    }.model
    if (withResult) {
      model.solution = LPModelResult(
        status = LPSolutionStatus.OPTIMAL,
        computationTime = 10,
        mipGap = null,
        objective = 0.0,
      )
      model.variables.get("X")?.populateResult(1)
      model.variables.get("Y")?.populateResult(1)
      model.variables.get("Z")?.populateResult(0)
    }
    return model
  }

  private fun modelsForTest() = Stream.of(
    Arguments.of(
      "Sample model to JSON",
      sampleModel(false),
      "testModel.json",
      LPModelParser(LPModelFormat.JSON)
    ),
    Arguments.of(
      "Sample model to YAML",
      sampleModel(false),
      "testModel.yaml",
      LPModelParser(LPModelFormat.YAML)
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("modelsForTest")
  fun testReadWriteOfPrimitiveModel(testCase: String, model: LPModel, fileName: String, lpModelParser: LPModelParser) {
    log.info { "Test Case $testCase" }
    log.info { "Generating solver sample to write to file in JSON format: $fileName" }
    lpModelParser.writeToFile(model, fileName)
    Assertions.assertTrue(File(fileName).exists(), "JSON output was correctly written to file")
    log.info { "Reading model from file in JSON format" }
    val newModel = lpModelParser.readFromFile(fileName)
    Assertions.assertNotNull(newModel, "Model was successfully parsed from file")
    Assertions.assertEquals(
      newModel!!.identifier, model.identifier,
      "Identifiers for the model were re-parsed correctly"
    )
    Assertions.assertEquals(
      newModel.variables, model.variables,
      "Variables for the model were re-parsed correctly"
    )
    Assertions.assertEquals(
      newModel.constants, model.constants,
      "Constants for the model were re-parsed correctly"
    )
    Assertions.assertEquals(
      newModel.constraints, model.constraints,
      "Constraints for the model were re-parsed correctly"
    )
    Assertions.assertEquals(
      newModel.objective, model.objective,
      "Objective fn for the model were re-parsed correctly"
    )
    Assertions.assertEquals(
      newModel, model,
      "Models generated are equivalent"
    )
  }

  private fun modelsForTestWithResult() = Stream.of(
    Arguments.of(
      "Sample model to JSON",
      sampleModel(true),
      "testModel.json",
      "testModel-result.json",
      LPModelParser(LPModelFormat.JSON)
    ),
    Arguments.of(
      "Sample model to YAML",
      sampleModel(true),
      "testModel.yaml",
      "testModel-result.yaml",
      LPModelParser(LPModelFormat.YAML)
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("modelsForTestWithResult")
  fun testReadWriteOfPrimitiveModelWithResult(
    testCase: String,
    model: LPModel,
    fileName: String,
    resultFileName: String,
    lpModelParser: LPModelParser
  ) {
    log.info { "Test Case $testCase" }
    log.info { "Generating solver sample to write to file in JSON format: $fileName" }
    lpModelParser.writeToFile(model, fileName)
    lpModelParser.writeResultToFile(model, resultFileName)
    Assertions.assertTrue(File(fileName).exists(), "JSON output was correctly written to file")
    Assertions.assertTrue(File(resultFileName).exists(), "JSON result output was correctly written to file")
    log.info { "Reading model from file in JSON format" }
    val newModel = lpModelParser.readFromFile(fileName)
    Assertions.assertNotEquals(
      newModel, model,
      "Models with result set should not be equal to the same model without results"
    )
    lpModelParser.readResultFromFile(resultFileName, newModel!!)
    Assertions.assertEquals(
      newModel, model,
      "Models once written to file, and re-parsed into a separate object should be equal"
    )
  }

  private fun resultDtosForTest() = Stream.of(
    Arguments.of(
      "DTO with computed false is parsed without errors",
      LPModelParser(LPModelFormat.JSON),
      sampleModel(false),
      LPModelResultDto(computed = false), true
    ),
    Arguments.of(
      "DTO with null status results in false",
      LPModelParser(LPModelFormat.JSON),
      sampleModel(false),
      LPModelResultDto(computed = true, status = null), false
    ),
    Arguments.of(
      "DTO with objective but no variables results in false",
      LPModelParser(LPModelFormat.JSON),
      sampleModel(false),
      LPModelResultDto(computed = true, status = LPSolutionStatus.OPTIMAL, objective = 1.0), false
    ),
    Arguments.of(
      "DTO with bad variable identifier results in false",
      LPModelParser(LPModelFormat.JSON),
      sampleModel(false),
      LPModelResultDto(
        computed = true, status = LPSolutionStatus.OPTIMAL, objective = 1.0,
        vars = listOf(
          LPVarResultDto("some-other-var", 1),
        )
      ),
      false
    ),
    Arguments.of(
      "DTO with correct variables results in true",
      LPModelParser(LPModelFormat.JSON),
      sampleModel(false),
      LPModelResultDto(
        computed = true, status = LPSolutionStatus.OPTIMAL, objective = 0.0,
        vars = listOf(
          LPVarResultDto("X", 1),
          LPVarResultDto("Y", 1),
          LPVarResultDto("Z", 0),
        )
      ),
      true
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("resultDtosForTest")
  fun testPopulateModelResult(
    testCase: String,
    parser: LPModelParser,
    model: LPModel,
    resultDto: LPModelResultDto,
    expected: Boolean
  ) {
    log.info { "Executing: $testCase" }
    val got = parser.populateModelResult(model, resultDto)
    Assertions.assertEquals(
      got, expected,
      "parser.populateModelResult($model, $resultDto) want :$expected, got $got"
    )
  }

  private fun writeToFileTestCases() = Stream.of(
    Arguments.of("JSON Parser", sampleModel(true), LPModelParser(LPModelFormat.JSON)),
    Arguments.of("YAML Parser", sampleModel(true), LPModelParser(LPModelFormat.YAML)),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("writeToFileTestCases")
  fun testWriteToInvalidLocation(testCase: String, model: LPModel, parser: LPModelParser) {
    log.info { "Test Case: $testCase" }
    val got = parser.writeToFile(model, "")
    Assertions.assertFalse(got, "Writing to invalid location should result in false")
    val gotResult = parser.writeResultToFile(model, "")
    Assertions.assertFalse(gotResult, "Writing results to invalid location should result in false")
  }

  private fun readFromFileTestCases() = Stream.of(
    Arguments.of("JSON Parser", sampleModel(false), LPModelParser(LPModelFormat.JSON)),
    Arguments.of("YAML Parser", sampleModel(false), LPModelParser(LPModelFormat.YAML)),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("readFromFileTestCases")
  fun testReadFromInvalidLocation(testCase: String, model: LPModel, parser: LPModelParser) {
    log.info { "Test Case: $testCase" }
    val newModel = parser.readFromFile("")
    Assertions.assertNull(newModel, "Reading from invalid location should result in null")
    val gotResult = parser.readResultFromFile("", model)
    Assertions.assertFalse(gotResult, "Reading results from invalid location should result in false")
  }
}