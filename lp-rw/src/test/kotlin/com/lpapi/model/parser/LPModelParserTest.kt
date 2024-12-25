package com.lpapi.model.parser

import com.lpapi.model.LPModel
import com.lpapi.solver.sample.PrimitiveSolverSample
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LPModelParserTest : PrimitiveSolverSample() {

  private fun testReadWriteOfPrimitiveModel(fileName: String, lpModelParser: LPModelParser) {
    log.info { "Generating solver sample to write to file in JSON format: $fileName" }
    lpModelParser.writeToFile(initLpModel(), fileName)
    assertTrue(File(fileName).exists(), "JSON output was correctly written to file")
    log.info { "Reading model from file in JSON format" }
    val newModel = lpModelParser.readFromFile(fileName)
    assertNotNull(newModel, "Model was successfully parsed from file")
    //TODO add checkers to see if the constraints, constants and variables were initialized correctly
  }

  @Test
  fun testReadWriteToJson() {
    testReadWriteOfPrimitiveModel("testModel.json", LPModelParser(LPModelFormat.JSON))
  }

  @Test
  fun testReadWriteToYaml() {
    testReadWriteOfPrimitiveModel("testModel.yaml", LPModelParser(LPModelFormat.YAML))
  }

  override fun initAndSolveModel(model: LPModel): LPModel? {
    //not required
    return null
  }
}