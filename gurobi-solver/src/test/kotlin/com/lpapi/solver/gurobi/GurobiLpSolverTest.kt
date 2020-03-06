package com.lpapi.solver.gurobi

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType
import gurobi.GRB
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GurobiLpSolverTest {

  @Test
  fun testGrbVarTypeComputation() {
    val model = GurobiLpSolver(LPModel())
    LPVarType.values().forEach { lpVarType ->
      assertNotNull(model.getGurobiVarType(lpVarType), "Gurobi Model variable type not found for LP type $lpVarType")
    }
  }

  @Test
  fun testGrbOperatorTypeComputation() {
    val model = GurobiLpSolver(LPModel())
    LPOperator.values().forEach { lpOperator ->
      assertNotNull(model.getGurobiOperator(lpOperator), "Gurobi Model operator type not found for LP Operator $lpOperator")
    }
  }

  @Test
  fun testGrbObjectiveType() {
    val model = GurobiLpSolver(LPModel())
    LPObjectiveType.values().forEach { lpObjectiveType ->
      assertNotNull(model.getGurobiObjectiveType(lpObjectiveType), "Gurobi Model Objective Type node found for LP Objective Type $lpObjectiveType")
    }
  }

  @Test
  fun testGrbSolutionStatusTest() {
    val solutionStatusMap: Map<Int?, LPSolutionStatus> = mapOf(
        Pair(GRB.Status.OPTIMAL, LPSolutionStatus.OPTIMAL),
        Pair(GRB.Status.UNBOUNDED, LPSolutionStatus.UNBOUNDED),
        Pair(GRB.Status.INFEASIBLE, LPSolutionStatus.INFEASIBLE),
        Pair(GRB.Status.INF_OR_UNBD, LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED),
        Pair(GRB.Status.TIME_LIMIT, LPSolutionStatus.TIME_LIMIT),
        Pair(GRB.Status.CUTOFF, LPSolutionStatus.CUTOFF),
        Pair(null, LPSolutionStatus.ERROR),
        Pair(GRB.Status.INPROGRESS, LPSolutionStatus.UNKNOWN),
        Pair(GRB.Status.INTERRUPTED, LPSolutionStatus.UNKNOWN),
        Pair(GRB.Status.ITERATION_LIMIT, LPSolutionStatus.UNKNOWN),
        Pair(GRB.Status.NODE_LIMIT, LPSolutionStatus.UNKNOWN),
        Pair(GRB.Status.SOLUTION_LIMIT, LPSolutionStatus.UNKNOWN),
        Pair(GRB.Status.USER_OBJ_LIMIT, LPSolutionStatus.UNKNOWN)
    )

    val solver = GurobiLpSolver(LPModel())
    solutionStatusMap.entries.forEach { entry ->
      assertEquals(entry.value, solver.getSolutionStatus(entry.key),
          "Gurobi status ${entry.key} not translated correctly to ${entry.value}")
    }
  }
}
