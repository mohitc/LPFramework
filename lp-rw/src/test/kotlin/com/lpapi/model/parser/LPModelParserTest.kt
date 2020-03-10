package com.lpapi.model.parser

import com.lpapi.model.LPModel
import com.lpapi.solver.sample.PrimitiveSolverSample
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LPModelParserTest {

  private val log = KotlinLogging.logger("LPModelParserTest")

  private val modelList: List<LPModel> = listOf(
      object: PrimitiveSolverSample() {
        override fun initAndSolveModel(model: LPModel): LPModel? = null
      }.model)

  private fun testReadWriteOfPrimitiveModel(model: LPModel, fileName: String, lpModelParser: LPModelParser) {
    log.info { "Generating solver sample to write to file in JSON format: $fileName" }
    lpModelParser.writeToFile(model, fileName)
    assertTrue(File(fileName).exists(), "JSON output was correctly written to file")
    log.info { "Reading model from file in JSON format" }
    val newModel = lpModelParser.readFromFile(fileName)
    assertNotNull(newModel, "Model was successfully parsed from file")
    //TODO add checkers to see if the constraints, constants and variables were initialized correctly
  }

  @Test
  fun testReadWriteToJson() {
    modelList.forEach{ model -> testReadWriteOfPrimitiveModel(model, "testModel.json", LPModelParser(LPModelFormat.JSON)) }
  }

  @Test
  fun testReadWriteToYaml() {
    modelList.forEach {model -> testReadWriteOfPrimitiveModel(model, "testModel.yaml", LPModelParser(LPModelFormat.YAML)) }
  }

}