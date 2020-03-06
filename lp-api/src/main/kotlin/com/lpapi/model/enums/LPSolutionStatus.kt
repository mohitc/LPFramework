package com.lpapi.model.enums

enum class LPSolutionStatus {
  OPTIMAL,
  INFEASIBLE,
  UNBOUNDED,
  TIME_LIMIT,
  INFEASIBLE_OR_UNBOUNDED,
  CUTOFF,
  BOUNDED,
  UNKNOWN,
  ERROR;
}