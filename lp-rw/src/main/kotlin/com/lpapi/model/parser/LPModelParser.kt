package com.lpapi.model.parser

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.lpapi.model.*
import com.lpapi.model.dto.*
import com.lpapi.model.enums.LPSolutionStatus
import mu.KotlinLogging
import java.io.File


class LPModelParser (format: LPModelFormat = LPModelFormat.YAML) {

  private val log = KotlinLogging.logger("LPModelParser")

  private val mapper : ObjectMapper = if (format == LPModelFormat.YAML) ObjectMapper(YAMLFactory()) //Enable YAML Parsing if required
  else ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT) //Enable Pretty printing for JSON

  init {
    //Configuration for the ObjectMapper
    mapper.registerModule(KotlinModule())
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  }

  /**Function to generate the DTO object for an existing LP model */
  fun generateModelDto(model: LPModel) : LPModelDto {

    val constantMap: Map<String, List<LPConstantDto>> =
        model.constants.grouping.map {entry ->
          (entry.key to entry.value.toList()
              .map { constant -> LPConstantDto(constant, model.constants.get(constant)?.value!!) })}
            .toMap()
    val varMap: Map<String, List<LPVarDto>> =
        model.variables.grouping.map {entry ->
          (entry.key to entry.value.toList()
              .map { lpVarIdentifier -> model.variables.get(lpVarIdentifier)!!}
              .map {v-> LPVarDto(v.identifier, v.type, v.lbound, v.ubound) }
              )
        }.toMap()

    val constraintMap : Map<String, List<LPConstraintDto>> =
        model.constraints.grouping.map {entry ->
          (entry.key to entry.value.toList()
              .map { lpConstraintIdentifier-> model.constraints.get(lpConstraintIdentifier)!!}
              .map {v-> LPConstraintDto(v.identifier, generateExpressionDto(v.lhs), v.operator, generateExpressionDto(v.rhs)) }
              )
        }.toMap()

    return LPModelDto(model.identifier, model.objective.objective, generateExpressionDto(model.objective.expression),
        constantMap, varMap, constraintMap)
  }

  /** Function to create the LP Model from a DTO */
  fun generateModel(dto: LPModelDto) : LPModel {
    val model = LPModel(dto.identifier)

    //Initialize constants
    dto.constants.entries.forEach { mapEntry ->
      mapEntry.value.forEach{ constantDto ->
        model.constants.add(mapEntry.key, LPConstant(constantDto.identifier, constantDto.value)) }}

    //Initialize variables
    dto.vars.entries.forEach { mapEntry ->
      mapEntry.value.forEach{ varDto ->
        model.variables.add(mapEntry.key, LPVar(varDto.identifier, varDto.type, varDto.lbound, varDto.ubound)) }}

    //Initialize constraints
    dto.constraints.entries.forEach { mapEntry ->
      mapEntry.value.forEach{ constraintDto ->
        model.constraints.add(mapEntry.key, LPConstraint(constraintDto.identifier, generateExpression(constraintDto.lhs), constraintDto.operator, generateExpression(constraintDto.rhs))) }}

    //Initialize Objective function
    model.objective.objective = dto.objectiveType
    model.objective.expression = generateExpression(dto.objective)

    return model
  }

  /**Function to convert the model to the corresponding DTO and write to file */
  fun writeToFile(lpModel : LPModel, fileName: String) : Boolean {
    return try {
      mapper.writeValue(File(fileName), generateModelDto(lpModel))
      true
    } catch (e: Exception) {
      log.error("Error while writing model to file $fileName : $e")
      false
    }
  }

  /**Function to read the DTO from a file, parse the DTO, and create an LP model from the parsed data*/
  fun readFromFile(fileName: String) : LPModel? {
    try {
      val modelDto: LPModelDto = mapper.readValue(File(fileName), LPModelDto::class.java)
      log.info { "Model Dto parsed successfully. Generating LPModel" }
      return generateModel(modelDto)
    } catch (e: Exception) {
      log.error("Error while generating model from file $fileName : $e")
    }
    return null
  }

  /**Funtion to generate the Result DTO from the LP Model*/
  fun generateModelResultDto(model: LPModel) : LPModelResultDto {
    val solution = model.solution
    return if (solution==null) {
      LPModelResultDto(false)
    } else {
      //Solution was computed. Set the parameters accordingly
      //If the objective is set, then the model is present and the results are likely set
      if (solution.objective != null) {
        LPModelResultDto(true, solution.status, solution.objective, solution.computationTime, solution.mipGap,
            model.variables.allValues().toList().map { lpVar -> LPVarResultDto(lpVar.identifier, lpVar.result) }
        )
      } else {
        LPModelResultDto(true, solution.status, solution.objective, solution.computationTime, solution.mipGap)
      }
    }
  }

  /**Function to populate the model result into the LP model from the LPModelResultDTO, returns
   * true in case everything went okay otherwise returns false*/
  fun populateModuleResult(model: LPModel, resultDto: LPModelResultDto) : Boolean {
    if (resultDto.computed) {
      if (resultDto.status===null) {
        log.error { "Model computed but solution status is unknown" }
        return false
      }
      model.solution = LPModelResult(resultDto.status, resultDto.objective, resultDto.computationTime, resultDto.mipGap)
      if (resultDto.objective!=null) {
        //variable results must be set
        if (resultDto.vars==null) {
          log.error { "result has objective value but no information on the values of the variables" }
          return false
        }
        resultDto.vars.forEach { lpVarResult -> run {
          val lpVar = model.variables.get(lpVarResult.identifier)
          if (lpVar != null) {
            lpVar.populateResult(lpVarResult.result)
          } else {
            log.error { "result has variable ${lpVarResult.identifier} but variable not found in model" }
            return false
          }
        }}
      }
    }
    return true
  }

  /**Function to convert the model to the corresponding Result DTO and write to file */
  fun writeResultToFile(lpModel : LPModel, fileName: String) : Boolean {
    return try {
      mapper.writeValue(File(fileName), generateModelResultDto(lpModel))
      true
    } catch (e: Exception) {
      log.error("Error while writing model result to file $fileName : $e")
      false
    }
  }

  /**Function to read the DTO from a file, parse the DTO, and create an LP model from the parsed data*/
  fun readResultFromFile(fileName: String, model: LPModel) : Boolean {
    try {
      val modelResultDto: LPModelResultDto = mapper.readValue(File(fileName), LPModelResultDto::class.java)
      log.info { "Model Result Dto parsed successfully.Populating results into provided LPModel" }
      return populateModuleResult(model, modelResultDto)
    } catch (e: Exception) {
      log.error("Error while generating model from file $fileName : $e")
    }
    return false
  }

  /** Function to generate the DTO from a model expression
   */
  internal fun generateExpressionDto(lpExpression: LPExpression) : LPExpressionDto {
    return LPExpressionDto(lpExpression.expression
        .map { term -> LPExpressionTermDto(term.coefficient, term.lpVarIdentifier, term.lpConstantIdentifier) })
  }

  /** Function to generate the model expression from the provided DTO
   */
  internal fun generateExpression(lpExpressionDto: LPExpressionDto) : LPExpression {
    val lpExpression = LPExpression()
    lpExpressionDto.terms.forEach{termDto -> lpExpression.expression.add(LPExpressionTerm(termDto.coefficient, termDto.varName, termDto.constant))}
    return lpExpression
  }

}


/** Formats in which data can be exported
 *
 */
enum class LPModelFormat {
  YAML, JSON
}