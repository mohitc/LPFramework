package com.lpapi.solver.cplex

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType
import ilog.cplex.IloCplex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class CplexLpSolverConverterTest {
  @Test
  fun testGetCplexVarType() {
    val solver = CplexLpSolver(LPModel())
    LPVarType.values().forEach { v ->
      assertNotNull(solver.getCplexVarType(v), "No equivalent variable type associated with LPVarType $v")
    }
  }

  @Test
  fun testGetCplexSolutionStatus() {
    val solutionStatusMap: Map<IloCplex.Status?, LPSolutionStatus> =
      mapOf(
        Pair(IloCplex.Status.Optimal, LPSolutionStatus.OPTIMAL),
        Pair(IloCplex.Status.Unbounded, LPSolutionStatus.UNBOUNDED),
        Pair(IloCplex.Status.Infeasible, LPSolutionStatus.INFEASIBLE),
        Pair(IloCplex.Status.InfeasibleOrUnbounded, LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED),
        Pair(IloCplex.Status.Bounded, LPSolutionStatus.BOUNDED),
        Pair(IloCplex.Status.Error, LPSolutionStatus.ERROR),
        Pair(IloCplex.Status.Feasible, LPSolutionStatus.UNKNOWN),
        Pair(IloCplex.Status.Unknown, LPSolutionStatus.UNKNOWN),
        Pair(null, LPSolutionStatus.ERROR),
      )

    val solver = CplexLpSolver(LPModel())
    solutionStatusMap.entries.forEach { entry ->
      assertEquals(
        entry.value,
        solver.getSolutionStatus(entry.key),
        "CPLEX status ${entry.key} not translated correctly to ${entry.value}",
      )
    }
  }
}