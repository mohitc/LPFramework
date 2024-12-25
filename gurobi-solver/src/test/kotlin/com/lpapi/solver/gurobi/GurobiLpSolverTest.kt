package com.lpapi.solver.gurobi

import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRBConstr
import com.gurobi.gurobi.GRBException
import com.gurobi.gurobi.GRBLinExpr
import com.gurobi.gurobi.GRBModel
import com.gurobi.gurobi.GRBVar
import com.lpapi.model.LPConstraint
import com.lpapi.model.LPModel
import com.lpapi.model.LPModelResult
import com.lpapi.model.LPVar
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.same
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.stream.Stream
import kotlin.math.roundToInt

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GurobiLpSolverTest {
  private val log = KotlinLogging.logger { this.javaClass.name }

  companion object {
    val mockedVar = mock<GRBVar> {}
    val mockedConstraint = mock<GRBConstr> {}
  }

  private fun setParameter(
    solver: GurobiLpSolver,
    field: String,
    value: Any?,
  ) {
    solver.javaClass.getDeclaredField(field).let {
      it.isAccessible = true
      it.set(solver, value)
    }
  }

  private fun setModel(
    solver: GurobiLpSolver,
    model: GRBModel?,
  ) = setParameter(solver, "grbModel", model)

  private fun setVariableMap(
    solver: GurobiLpSolver,
    variableMap: MutableMap<String, GRBVar>,
  ) = setParameter(solver, "variableMap", variableMap)

  private fun setConstraintMap(
    solver: GurobiLpSolver,
    variableMap: MutableMap<String, GRBConstr>,
  ) = setParameter(solver, "constraintMap", variableMap)

  @Test
  fun testGetBaseModel() {
    val solver = GurobiLpSolver(LPModel("test"))
    val grbModel = mock<GRBModel> {}
    setModel(solver, grbModel)
    assertEquals(grbModel, solver.getBaseModel(), "solver.getBaseModel()")
  }

  @Test
  fun testGrbVarTypeComputation() {
    val model = GurobiLpSolver(LPModel())
    LPVarType.values().forEach { lpVarType ->
      assertNotNull(
        model.getGurobiVarType(lpVarType),
        "Gurobi Model variable type not found for LP " +
          "type $lpVarType",
      )
    }
  }

  @Test
  fun testGrbOperatorTypeComputation() {
    val model = GurobiLpSolver(LPModel())
    LPOperator.values().forEach { lpOperator ->
      assertNotNull(
        model.getGurobiOperator(lpOperator),
        "Gurobi Model operator type not found for LP " +
          "Operator $lpOperator",
      )
    }
  }

  @Test
  fun testGrbObjectiveType() {
    val model = GurobiLpSolver(LPModel())
    LPObjectiveType.values().forEach { lpObjectiveType ->
      assertNotNull(
        model.getGurobiObjectiveType(lpObjectiveType),
        "Gurobi Model Objective Type node found " +
          "for LP Objective Type $lpObjectiveType",
      )
    }
  }

  @Test
  fun testGrbSolutionStatusTest() {
    val solutionStatusMap: Map<Int?, LPSolutionStatus> =
      mapOf(
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
        Pair(GRB.Status.USER_OBJ_LIMIT, LPSolutionStatus.UNKNOWN),
      )

    val solver = GurobiLpSolver(LPModel())
    solutionStatusMap.entries.forEach { entry ->
      assertEquals(
        entry.value,
        solver.getSolutionStatus(entry.key),
        "Gurobi status ${entry.key} not translated correctly to ${entry.value}",
      )
    }
  }

  private fun argsForInitVars() =
    Stream.of(
      Arguments.of(
        "null model results in false",
        null,
        LPVar("x", LPVarType.BOOLEAN),
        false,
        mutableMapOf<String, GRBVar>(),
      ),
      Arguments.of(
        "Add variable results in an exception",
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenThrow(GRBException("exception"))
        },
        LPVar("x", LPVarType.BOOLEAN),
        false,
        mutableMapOf<String, GRBVar>(),
      ),
      Arguments.of(
        "Add variable results in a null value",
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(null)
        },
        LPVar("x", LPVarType.BOOLEAN),
        false,
        mutableMapOf<String, GRBVar>(),
      ),
      Arguments.of(
        "Boolean variable added successfully",
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedVar)
        },
        LPVar("x", LPVarType.BOOLEAN),
        true,
        mutableMapOf(Pair("x", mockedVar)),
      ),
      Arguments.of(
        "Integer variable added successfully",
        mock<GRBModel> {
          on { addVar(1.0, 10.0, 0.0, GRB.INTEGER, "y") }.thenReturn(mockedVar)
        },
        LPVar("y", LPVarType.INTEGER, 1, 10),
        true,
        mutableMapOf(Pair("y", mockedVar)),
      ),
      Arguments.of(
        "Continuous variable added successfully",
        mock<GRBModel> {
          on { addVar(3.2, 5.7, 0.0, GRB.CONTINUOUS, "z") }.thenReturn(mockedVar)
        },
        LPVar("z", LPVarType.DOUBLE, 3.2, 5.7),
        true,
        mutableMapOf(Pair("z", mockedVar)),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitVars")
  fun testInitVars(
    desc: String,
    model: GRBModel?,
    lpVar: LPVar,
    wantSuccess: Boolean,
    wantVarMap: Map<String, GRBVar>,
  ) {
    log.info { "Test case: $desc" }
    val lpModel = LPModel("test").apply { this.variables.add(lpVar) }
    val solver = GurobiLpSolver(lpModel).apply { setModel(this, model) }
    val gotVarMap = mutableMapOf<String, GRBVar>().apply { setVariableMap(solver, this) }
    val gotSuccess = solver.initVars()
    assertEquals(wantSuccess, gotSuccess, "solver.initVars()")
    assertEquals(gotVarMap, wantVarMap, "solver.variableMap")
  }

  private fun argsForInitObjectiveFunction() =
    Stream.of(
      Arguments.of(
        "Irreducible objective results in error",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.objective.expression
            .addTerm("a", "x")
            .add("b")
        },
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedVar)
        },
        false,
        null,
        null,
      ),
      Arguments.of(
        "Exception on setObjective results in false",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.objective.expression
            .addTerm(2, "x")
            .add(3)
        },
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedVar)
          on { setObjective(any(), any()) }.thenThrow(GRBException(""))
        },
        false,
        null,
        null,
      ),
      Arguments.of(
        "Objective is initialized correctly (Maximize)",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.objective.objective = LPObjectiveType.MAXIMIZE
          this.objective.expression
            .addTerm(2, "x")
            .add(3)
        },
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedVar)
        },
        true,
        GrbExprSummary(3.0, mapOf(Pair(mockedVar, 2.0))),
        GRB.MAXIMIZE,
      ),
      Arguments.of(
        "Objective is initialized correctly (Minimize)",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.objective.objective = LPObjectiveType.MINIMIZE
          this.objective.expression
            .addTerm(3, "x")
            .add(2)
        },
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedVar)
        },
        true,
        GrbExprSummary(2.0, mapOf(Pair(mockedVar, 3.0))),
        GRB.MINIMIZE,
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitObjectiveFunction")
  fun testInitObjectiveFunction(
    desc: String,
    lpModel: LPModel,
    grbModel: GRBModel,
    wantSuccess: Boolean,
    wantSummary: GrbExprSummary?,
    wantDirection: Int?,
  ) {
    log.info { "Test case: $desc" }
    val solver = GurobiLpSolver(lpModel)
    setModel(solver, grbModel)
    solver.initVars()
    val gotSuccess = solver.initObjectiveFunction()
    assertEquals(wantSuccess, gotSuccess, "solver.initObjectiveFunction()")
    if (!wantSuccess) {
      return
    }
    // validate parameters
    val exprCaptor = argumentCaptor<GRBLinExpr>()
    val directionCaptor = argumentCaptor<Int>()
    verify(grbModel).setObjective(exprCaptor.capture(), directionCaptor.capture())
    assertEquals(wantDirection, directionCaptor.firstValue, "Optimization direction")
    val gotSummary = GrbExprSummary(exprCaptor.firstValue)
    assertEquals(wantSummary, gotSummary, "Expression")
  }

  private fun argsForInitConstraints(): Stream<Arguments> {
    val mockedX = mock<GRBVar> {}
    val mockedY = mock<GRBVar> {}

    return Stream.of(
      Arguments.of(
        "Irreducible constraint results in false",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              // generate constraint ax >= 3 - by
              this.lhs.addTerm("a", "x")
              this.operator = LPOperator.GREATER_EQUAL
              this.rhs.add(3).addTerm("b", "y")
            },
          )
        },
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedX)
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "y") }.thenReturn(mockedY)
          on { addConstr(any<GRBLinExpr>(), any(), any<GRBLinExpr>(), same("test-constraint")) }
            .thenReturn(mockedConstraint)
        },
        false,
        mutableMapOf<String, GRBConstr>(),
        null,
        null,
        null,
        null,
      ),
      Arguments.of(
        "Null base model results in false",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              // generate constraint 2x >= 3 + 4y
              this.lhs.addTerm(2, "x")
              this.operator = LPOperator.GREATER_EQUAL
              this.rhs.add(3).addTerm(4, "y")
            },
          )
        },
        null,
        false,
        mutableMapOf<String, GRBConstr>(),
        null,
        null,
        null,
        null,
      ),
      Arguments.of(
        "Null on addConstr() results in false",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              // generate constraint 2x >= 3 + 4y
              this.lhs.addTerm(2, "x")
              this.operator = LPOperator.GREATER_EQUAL
              this.rhs.add(3).addTerm(4, "y")
            },
          )
        },
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedX)
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "y") }.thenReturn(mockedY)
        },
        false,
        mutableMapOf<String, GRBConstr>(),
        null,
        null,
        null,
        null,
      ),
      Arguments.of(
        "Exception on addConstr() results in false",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              // generate constraint ax >= 3 - by
              this.lhs.addTerm(2, "x")
              this.operator = LPOperator.GREATER_EQUAL
              this.rhs.add(3).addTerm(4, "y")
            },
          )
        },
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedX)
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "y") }.thenReturn(mockedY)
          on { addConstr(any<GRBLinExpr>(), any(), any<GRBLinExpr>(), same("test-constraint")) }
            .thenThrow(GRBException(""))
        },
        false,
        mutableMapOf<String, GRBConstr>(),
        null,
        null,
        null,
        null,
      ),
      Arguments.of(
        "Success with <= operation",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              // generate constraint 2x <= 3 + 4y
              this.lhs.addTerm(2, "x")
              this.operator = LPOperator.LESS_EQUAL
              this.rhs.add(3).addTerm(4, "y")
            },
          )
        },
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedX)
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "y") }.thenReturn(mockedY)
          on { addConstr(any<GRBLinExpr>(), any(), any<GRBLinExpr>(), same("test-constraint")) }
            .thenReturn(mockedConstraint)
        },
        true,
        mutableMapOf(Pair("test-constraint", mockedConstraint)),
        GrbExprSummary(0.0, mapOf(Pair(mockedX, 2.0))),
        GRB.LESS_EQUAL,
        GrbExprSummary(3.0, mapOf(Pair(mockedY, 4.0))),
        "test-constraint",
      ),
      Arguments.of(
        "Success with == operation",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              // generate constraint 3y = 3 + 4x
              this.lhs.addTerm(3, "y")
              this.operator = LPOperator.EQUAL
              this.rhs.add(3).addTerm(4, "x")
            },
          )
        },
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedX)
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "y") }.thenReturn(mockedY)
          on { addConstr(any<GRBLinExpr>(), any(), any<GRBLinExpr>(), same("test-constraint")) }
            .thenReturn(mockedConstraint)
        },
        true,
        mutableMapOf(Pair("test-constraint", mockedConstraint)),
        GrbExprSummary(0.0, mapOf(Pair(mockedY, 3.0))),
        GRB.EQUAL,
        GrbExprSummary(3.0, mapOf(Pair(mockedX, 4.0))),
        "test-constraint",
      ),
      Arguments.of(
        "Success with >= operation",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              // generate constraint 2 (x+y) + x >= 3 + 4x - 2y
              this.lhs
                .addTerm(2, "y")
                .addTerm(2, "x")
                .addTerm("x")
              this.operator = LPOperator.GREATER_EQUAL
              this.rhs
                .add(3)
                .addTerm(4, "x")
                .addTerm(-2, "y")
            },
          )
        },
        mock<GRBModel> {
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "x") }.thenReturn(mockedX)
          on { addVar(0.0, 1.0, 0.0, GRB.BINARY, "y") }.thenReturn(mockedY)
          on { addConstr(any<GRBLinExpr>(), any(), any<GRBLinExpr>(), same("test-constraint")) }
            .thenReturn(mockedConstraint)
        },
        true,
        mutableMapOf(Pair("test-constraint", mockedConstraint)),
        GrbExprSummary(0.0, mapOf(Pair(mockedX, 3.0), Pair(mockedY, 2.0))),
        GRB.GREATER_EQUAL,
        GrbExprSummary(3.0, mapOf(Pair(mockedX, 4.0), Pair(mockedY, -2.0))),
        "test-constraint",
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitConstraints")
  fun testInitConstraints(
    desc: String,
    lpModel: LPModel,
    grbModel: GRBModel?,
    wantSuccess: Boolean,
    wantConstraintMap: MutableMap<String, GRBConstr>,
    wantLhsSummary: GrbExprSummary?,
    wantOperator: Char?,
    wantRhsSummary: GrbExprSummary?,
    wantConstraintId: String?,
  ) {
    log.info { "Test Case: $desc" }
    val gotConstraintMap = mutableMapOf<String, GRBConstr>()
    val solver = GurobiLpSolver(lpModel)
    setModel(solver, grbModel)
    setConstraintMap(solver, gotConstraintMap)
    solver.initVars()
    val gotSuccess = solver.initConstraints()
    assertEquals(wantSuccess, gotSuccess, "solver.initConstraints()")
    assertEquals(wantConstraintMap, gotConstraintMap, "solver.constraintMap")
    if (!wantSuccess) {
      return
    }
    val lhsCaptor = argumentCaptor<GRBLinExpr>()
    val conditionCaptor = argumentCaptor<Char>()
    val rhsCaptor = argumentCaptor<GRBLinExpr>()
    val idCaptor = argumentCaptor<String>()

    verify(grbModel)?.addConstr(lhsCaptor.capture(), conditionCaptor.capture(), rhsCaptor.capture(), idCaptor.capture())
    assertEquals(wantOperator, conditionCaptor.firstValue, "Constraint Direction")
    assertEquals(wantConstraintId, idCaptor.firstValue, "Constraint Identifier")
    assertEquals(wantLhsSummary, GrbExprSummary(lhsCaptor.firstValue), "LHS Expression")
    assertEquals(wantRhsSummary, GrbExprSummary(rhsCaptor.firstValue), "RHS Expression")
  }

  private fun argsForSolve(): Stream<Arguments> {
    val xVal = 2.3
    val yVal = 4.5
    val mockedX =
      mock<GRBVar> {
        on { get(GRB.DoubleAttr.X) }.thenReturn(xVal)
      }
    val mockedY =
      mock<GRBVar> {
        on { get(GRB.DoubleAttr.X) }.thenReturn(yVal)
      }
    val erroringMockedVar =
      mock<GRBVar> {
        on { get(GRB.DoubleAttr.X) }.thenThrow(GRBException("error"))
      }
    val lpModel =
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.DOUBLE, 1, 5))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
      }

    return Stream.of(
      Arguments.of(
        "Exception in Optimize() results in error",
        lpModel,
        mock<GRBModel> {
          on { addVar(1.0, 5.0, 0.0, GRB.CONTINUOUS, "x") }.thenReturn(mockedX)
          on { addVar(0.0, 10.0, 0.0, GRB.INTEGER, "y") }.thenReturn(mockedY)
          on { optimize() }.thenThrow(GRBException("error"))
        },
        LPSolutionStatus.ERROR,
        LPModelResult(LPSolutionStatus.ERROR),
        mutableMapOf<String, Number>(),
      ),
      Arguments.of(
        "Error status results in error",
        lpModel,
        null,
        LPSolutionStatus.ERROR,
        LPModelResult(status = LPSolutionStatus.ERROR),
        mutableMapOf<String, Number>(),
      ),
      Arguments.of(
        "Unknown Return status results in error",
        lpModel,
        mock<GRBModel> {
          on { addVar(1.0, 5.0, 0.0, GRB.CONTINUOUS, "x") }.thenReturn(mockedX)
          on { addVar(0.0, 10.0, 0.0, GRB.INTEGER, "y") }.thenReturn(mockedY)
          on { get(GRB.IntAttr.Status) }.thenReturn(GRB.Status.INTERRUPTED)
        },
        LPSolutionStatus.UNKNOWN,
        LPModelResult(status = LPSolutionStatus.UNKNOWN),
        mutableMapOf<String, Number>(),
      ),
      Arguments.of(
        "Status with known result results in a result value",
        lpModel,
        mock<GRBModel> {
          on { addVar(1.0, 5.0, 0.0, GRB.CONTINUOUS, "x") }.thenReturn(mockedX)
          on { addVar(0.0, 10.0, 0.0, GRB.INTEGER, "y") }.thenReturn(mockedY)
          on { get(GRB.IntAttr.Status) }.thenReturn(GRB.Status.OPTIMAL)
          on { get(GRB.DoubleAttr.ObjVal) }.thenReturn(23.2)
          on { get(GRB.DoubleAttr.MIPGap) }.thenReturn(0.2)
        },
        LPSolutionStatus.OPTIMAL,
        LPModelResult(status = LPSolutionStatus.OPTIMAL, mipGap = 0.2, objective = 23.2, computationTime = null),
        mutableMapOf<String, Number>(Pair("x", xVal), Pair("y", yVal.roundToInt())),
      ),
      Arguments.of(
        "Error during result extraction should result in an error",
        lpModel,
        mock<GRBModel> {
          on { addVar(1.0, 5.0, 0.0, GRB.CONTINUOUS, "x") }.thenReturn(mockedX)
          on { addVar(0.0, 10.0, 0.0, GRB.INTEGER, "y") }.thenReturn(erroringMockedVar)
          on { get(GRB.IntAttr.Status) }.thenReturn(GRB.Status.OPTIMAL)
          on { get(GRB.DoubleAttr.ObjVal) }.thenReturn(23.2)
          on { get(GRB.DoubleAttr.MIPGap) }.thenReturn(0.2)
        },
        LPSolutionStatus.ERROR,
        LPModelResult(status = LPSolutionStatus.ERROR),
        mutableMapOf<String, Number>(),
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForSolve")
  fun testSolve(
    desc: String,
    lpModel: LPModel,
    grbModel: GRBModel?,
    wantStatus: LPSolutionStatus,
    wantResult: LPModelResult,
    wantResultMap: Map<String, Number>,
  ) {
    log.info { "Test Case: $desc" }
    val solver = GurobiLpSolver(lpModel)
    setModel(solver, grbModel)
    solver.initVars()
    val gotStatus = solver.solve()
    assertEquals(wantStatus, gotStatus, "solver.solve() status")
    assertEquals(wantResult.status, lpModel.solution?.status, "lpModel.solution.status")
    assertEquals(wantResult.objective, lpModel.solution?.objective, "lpModel.solution.objective")
    assertEquals(wantResult.mipGap, lpModel.solution?.mipGap, "lpModel.solution.mipGap")
    val gotResultMap =
      lpModel.variables.allValues().filter { it.resultSet }.associate {
        Pair(
          it.identifier,
          it.result,
        )
      }
    assertEquals(wantResultMap, gotResultMap, "solver.solve() resultMap ${lpModel.variables.allValues()}")
  }
}

class GrbExprSummary {
  val constant: Double
  val termMap: Map<GRBVar, Double>

  constructor(constant: Double, termMap: Map<GRBVar, Double>) {
    this.constant = constant
    this.termMap = termMap.toMap()
  }

  constructor(expr: GRBLinExpr) {
    this.constant = expr.constant
    val termMap = mutableMapOf<GRBVar, Double>()
    for (i in 0 until expr.size()) {
      val key = expr.getVar(i)
      var existingValue = 0.0
      if (termMap.containsKey(key)) {
        existingValue = termMap[key]!!
      }
      termMap[key] = expr.getCoeff(i) + existingValue
    }
    this.termMap = termMap.toMap()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GrbExprSummary

    if (constant != other.constant) return false
    if (termMap != other.termMap) return false

    return true
  }

  override fun hashCode(): Int {
    var result = constant.hashCode()
    result = 31 * result + termMap.hashCode()
    return result
  }
}