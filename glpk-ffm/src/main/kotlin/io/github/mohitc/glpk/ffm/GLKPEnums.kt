package io.github.mohitc.glpk.ffm

import org.glpk.java.GLPK

enum class GLPKObjective(
  val description: String,
  val value: Int,
) {
  MINIMIZE("MINIMIZE", GLPK.GLP_MIN()),
  MAXIMIZE("MAXIMIZE", GLPK.GLP_MAX()),
}

enum class GLPKVarKind(
  val description: String,
  val value: Int,
) {
  CONTINUOUS("CONTINUOUS", GLPK.GLP_CV()),
  BOOLEAN("BOOLEAN", GLPK.GLP_BV()),
  INTEGER("INTEGER", GLPK.GLP_IV()),
}

enum class GLPKBoundType(
  val description: String,
  val value: Int,
) {
  UNBOUNDED("UNBOUNDED", GLPK.GLP_FR()),
  LOWER_BOUNDED("LOWER_BOUNDED", GLPK.GLP_LO()),
  UPPER_BOUNDED("UPPER_UNBOUNDED", GLPK.GLP_UP()),
  DOUBLE_BOUNDED("BOUNDED (Lower and Upper)", GLPK.GLP_DB()),
  FIXED("FIXED", GLPK.GLP_FX()),
}

enum class GLPKMipStatus(
  val description: String,
  val value: Int,
) {
  UNDEFINED("UNDEFINED", GLPK.GLP_UNDEF()),
  OPTIMAL("OPTIMAL", GLPK.GLP_OPT()),
  FEASIBLE("FEASIBLE", GLPK.GLP_FEAS()),
  NOFEASIBLE("NOT FEASIBLE", GLPK.GLP_NOFEAS()),
}

enum class GLPKStatus(
  val description: String,
  val value: Int,
) {
  UNDEFINED("UNDEFINED", GLPK.GLP_UNDEF()),
  FEASIBLE("FEASIBLE", GLPK.GLP_FEAS()),
  INFEASIBLE("INFEASIBLE", GLPK.GLP_INFEAS()),
  NOFEASIBLE("NOT FEASIBLE", GLPK.GLP_NOFEAS()),
  OPTIMAL("OPTIMAL", GLPK.GLP_OPT()),
  UNBOUNDED("UNBOUNDED", GLPK.GLP_UNBND()),
}

enum class GLPKMessageLevel(
  val description: String,
  val value: Int,
) {
  MSG_OFF("OFF", GLPK.GLP_MSG_OFF()),
  MSG_DEBUG("DEBUG", GLPK.GLP_MSG_DBG()),
  MSG_ON("NORMAL", GLPK.GLP_MSG_ON()),
  MSG_ERROR("ERROR", GLPK.GLP_MSG_ERR()),
  MSG_ALL("ALL", GLPK.GLP_MSG_ALL()),
}

enum class GLPKFeatureStatus(
  val description: String,
  val value: Int,
) {
  ON("ON", GLPK.GLP_ON()),
  OFF("OFF", GLPK.GLP_OFF()),
}