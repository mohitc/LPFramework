package com.lpapi.solver.enums

enum class LPSolutionStatus {
  OPTIMAL,
  INFEASIBLE,
  UNBOUNDED,
  TIME_LIMIT,
  INFEASIBLE_OR_UNBOUNDED,
  CUTOFF,
  UNKNOWN;
}