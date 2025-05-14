package io.github.mohitc.highs.ffm

// From the kHighsStatus values
enum class HIGHSStatus(
  val description: String,
  val value: Int,
) {
  ERROR("Error", -1),
  OK("Ok", 0),
  WARNING("Warning", 1),
  ;

  companion object {
    fun fromValue(intVal: Int) = values().find { v -> v.value == intVal } ?: ERROR
  }
}

// from the kHighsVarType
enum class HIGHSVarType(
  val description: String,
  val value: Int,
) {
  CONTINUOUS("Continuous", 0),
  INTEGER("Integer", 1),
  SEMI_CONTINUOUS("Semi-continuous (continuous between range or 0)", 2),
  SEMI_INTEGER("Integer range or 0", 3),
  IMPLICIT_INTEGER("Implicit Integer", 4),
  ;

  companion object {
    fun fromValue(intVal: Int) = values().find { v -> v.value == intVal } ?: CONTINUOUS
  }
}

// from the KHighsObjSense
enum class HIGHSObjective(
  val description: String,
  val value: Int,
) {
  MINIMIZE("Minimize", 1),
  MAXIMIZE("Maximize", -1),
  ;

  companion object {
    fun fromValue(intVal: Int) = values().find { v -> v.value == intVal }
  }
}

// from the kHighsModelStatus
enum class HIGHSModelStatus(
  val description: String,
  val value: Int,
) {
  NOT_SET("kHighsModelStatusNotSet", 0),
  LOAD_ERROR("kHighsModelStatusLoadError", 1),
  MODEL_ERROR("kHighsModelStatusModelError", 2),
  PRESOLVE_ERROR("kHighsModelStatusPresolveError", 3),
  SOLVE_ERROR("kHighsModelStatusSolveError", 4),
  POSTSOLVE_ERROR("kHighsModelStatusPostsolveError", 5),
  MODEL_EMPTY("kHighsModelStatusModelEmpty", 6),
  OPTIMAL("kHighsModelStatusOptimal", 7),
  INFEASIBLE("kHighsModelStatusInfeasible", 8),
  UNBOUNDED_OR_INFEASIBLE("kHighsModelStatusUnboundedOrInfeasible", 9),
  UNBOUNDED("kHighsModelStatusUnbounded", 10),
  OBJECTIVE_BOUND("kHighsModelStatusObjectiveBound", 11),
  OBJECTIVE_TARGET("kHighsModelStatusObjectiveTarget", 12),
  TIME_LIMIT("kHighsModelStatusTimeLimit", 13),
  ITERATION_LIMIT("kHighsModelStatusIterationLimit", 14),
  UNKNOWN("kHighsModelStatusUnknown", 15),
  SOLUTION_LIMIT("kHighsModelStatusSolutionLimit", 16),
  INTERRUPT("kHighsModelStatusInterrupt", 17),
  ;

  companion object {
    fun fromValue(intVal: Int) = HIGHSModelStatus.values().find { v -> v.value == intVal } ?: NOT_SET
  }
}

// Created from kHighsInfoType parameters
enum class HIGHSInfoType(
  val value: Int,
) {
  INT64(-1),
  INT(1),
  DOUBLE(2),
  ;

  companion object {
    fun fromValue(intVal: Int) = HIGHSInfoType.values().find { v -> v.value == intVal }
  }
}

// created from kHighsCallbackDataOut parameters
enum class HIGHSInfoParam(
  val param: String,
) {
  LogType("log_type"),
  RunningTime("running_time"),
  SimplexIterationCount("simplex_iteration_count"),
  IpmIterationCount("ipm_iteration_count"),
  PdlpIterationCount("pdlp_iteration_count"),
  ObjectiveFunctionValue("objective_function_value"),
  MipNodeCount("mip_node_count"),
  MipTotalLpIterations("mip_total_lp_iterations"),
  MipPrimalBound("mip_primal_bound"),
  MipDualBound("mip_dual_bound"),
  MipGap("mip_gap"),
  MipSolution("mip_solution"),
  CutpoolNumCol("cutpool_num_col"),
  CutpoolNumCut("cutpool_num_cut"),
  CutpoolNumNz("cutpool_num_nz"),
  CutpoolStart("cutpool_start"),
  CutpoolIndex("cutpool_index"),
  CutpoolValue("cutpool_value"),
  CutpoolLower("cutpool_lower"),
  CutpoolUpper("cutpool_upper"),
}