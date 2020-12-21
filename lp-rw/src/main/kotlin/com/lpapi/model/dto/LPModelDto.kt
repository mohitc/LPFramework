package com.lpapi.model.dto

import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType

/**Data transfer objects for the lp model for reading from and writing to files */

data class LPVarDto(val identifier: String, val type: LPVarType, val lbound: Double, val ubound: Double)

data class LPConstantDto(val identifier: String, val value: Double)

data class LPExpressionTermDto(val coefficient: Double?, val varName: String?, val constant: String?)

data class LPExpressionDto(val terms: List<LPExpressionTermDto>)

data class LPConstraintDto(
  val identifier: String,
  val lhs: LPExpressionDto,
  val operator: LPOperator,
  val rhs: LPExpressionDto
)

data class LPModelDto(
  val identifier: String,
  val objectiveType: LPObjectiveType,
  val objective: LPExpressionDto,
  val constants: Map<String, List<LPConstantDto>>,
  val vars: Map<String, List<LPVarDto>>,
  val constraints: Map<String, List<LPConstraintDto>>
)

data class LPVarResultDto(val identifier: String, val result: Number)

data class LPModelResultDto(
  val computed: Boolean,
  val status: LPSolutionStatus? = null,
  val objective: Double? = null,
  val computationTime: Long? = null,
  val mipGap: Double? = null,
  val vars: List<LPVarResultDto>? = null
)