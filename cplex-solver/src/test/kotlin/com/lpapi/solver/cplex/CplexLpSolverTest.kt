package com.lpapi.solver.cplex

import com.lpapi.model.LPConstraint
import com.lpapi.model.LPExpression
import com.lpapi.model.LPModel
import com.lpapi.model.LPModelResult
import com.lpapi.model.LPVar
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType
import ilog.concert.IloConstraint
import ilog.concert.IloException
import ilog.concert.IloLinearNumExpr
import ilog.concert.IloNumVar
import ilog.concert.IloNumVarType
import ilog.cplex.CpxNumVar
import ilog.cplex.IloCplex
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CplexLpSolverTest {

  private val log = KotlinLogging.logger(CplexLpSolverTest::class.java.name)

  companion object {
    var nilObjective: IloLinearNumExpr? = null
    val mockIloNumVar = mock<CpxNumVar> {}
    val mockConstraint = mock<IloConstraint> {}
    val mockExpression = mock<IloLinearNumExpr> {}
  }

  private fun setParameter(solver: CplexLpSolver, field: String, value: Any?) {
    solver.javaClass.getDeclaredField(field).let {
      it.isAccessible = true
      it.set(solver, value)
    }
  }

  private fun getObjective(solver: CplexLpSolver): IloLinearNumExpr {
    solver.javaClass.getDeclaredField("cplexObjective").let { field ->
      field.isAccessible = true
      return field.get(solver).let { it as IloLinearNumExpr }
    }
  }

  private fun setCplexModel(solver: CplexLpSolver, model: IloCplex?) = setParameter(solver, "cplexModel", model)

  private fun setVariableMap(solver: CplexLpSolver, variableMap: MutableMap<String, IloNumVar>) =
    setParameter(solver, "variableMap", variableMap)

  private fun setConstraintMap(solver: CplexLpSolver, constraintMap: MutableMap<String, IloConstraint>) =
    setParameter(solver, "constraintMap", constraintMap)

  private fun argsForTestSolve() = Stream.of(
    Arguments.of(
      "null model results in an error",
      null,
      LPSolutionStatus.ERROR,
      LPModelResult(LPSolutionStatus.ERROR),
    ),
    Arguments.of(
      "exception results in an error",
      mock<IloCplex> { on { solve() } doThrow IloException() },
      LPSolutionStatus.ERROR,
      LPModelResult(LPSolutionStatus.ERROR),
    ),
    Arguments.of(
      "IloCplex.Status.Error results in an error",
      mock<IloCplex> { on { status } doReturn IloCplex.Status.Error },
      LPSolutionStatus.ERROR,
      LPModelResult(LPSolutionStatus.ERROR),
    ),
    Arguments.of(
      "IloCplex.Status.Infeasible results in Infeasible",
      mock<IloCplex> { on { status } doReturn IloCplex.Status.Infeasible },
      LPSolutionStatus.INFEASIBLE,
      LPModelResult(LPSolutionStatus.INFEASIBLE),
    ),
    Arguments.of(
      "IloCplex.Status.Unbounded results in Unbounded",
      mock<IloCplex> { on { status } doReturn IloCplex.Status.Unbounded },
      LPSolutionStatus.UNBOUNDED,
      LPModelResult(LPSolutionStatus.UNBOUNDED),
    ),
    Arguments.of(
      "IloCplex.Status.InfeasibleOrUnbounded results in INFEASIBLE_OR_UNBOUNDED",
      mock<IloCplex> { on { status } doReturn IloCplex.Status.InfeasibleOrUnbounded },
      LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED,
      LPModelResult(LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED),
    ),
    Arguments.of(
      "IloCplex.Status.Unknown results in UNKNOWN, and populates solution parameters",
      mock<IloCplex> {
        on { status } doReturn IloCplex.Status.Unknown
        on { mipRelativeGap } doReturn 2.0
        on { getValue(nilObjective) } doReturn 3.4
      },
      LPSolutionStatus.UNKNOWN,
      LPModelResult(status = LPSolutionStatus.UNKNOWN, mipGap = 2.0, objective = 3.4, computationTime = 0),
    ),
    Arguments.of(
      "IloCplex.getMipRelativeGap() exception results in an error",
      mock<IloCplex> {
        on { status } doReturn IloCplex.Status.Unknown
        on { mipRelativeGap } doThrow IloException()
        on { getValue(nilObjective) } doReturn 3.4
      },
      LPSolutionStatus.ERROR,
      LPModelResult(LPSolutionStatus.ERROR),
    ),
    Arguments.of(
      "IloCplex.getValue(IloLinearNumExpr) exception results in an error",
      mock<IloCplex> {
        on { status } doReturn IloCplex.Status.Unknown
        on { mipRelativeGap } doReturn 2.0
        on { getValue(nilObjective) } doThrow IloException()
      },
      LPSolutionStatus.ERROR,
      LPModelResult(LPSolutionStatus.ERROR),
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForTestSolve")
  fun testSolve(testCase: String, cplexMock: IloCplex?, wantStatus: LPSolutionStatus, wantResult: LPModelResult) {
    log.info { "Test Case: $testCase" }
    val lpModel = LPModel()
    val solver = CplexLpSolver(lpModel)
    setCplexModel(solver, cplexMock)
    val gotStatus = solver.solve()
    assertEquals(wantStatus, gotStatus, "solver.solve()")

    assertEquals(wantResult.status, lpModel.solution?.status, "lpModel.solution.status")
    assertEquals(wantResult.objective, lpModel.solution?.objective, "lpModel.solution.objective")
    assertEquals(wantResult.mipGap, lpModel.solution?.mipGap, "lpModel.solution.mipGap")
  }

  private fun argsForTestExtractResult() = Stream.of(
    Arguments.of(
      "model with infeasible solution does not populate variable values",
      LPVar("x", LPVarType.BOOLEAN, 0, 1),
      mock<IloCplex> {
        on { status } doReturn IloCplex.Status.Infeasible
        on { getValue(mockIloNumVar) } doReturn 0.9
      },
      mutableMapOf(Pair("x", mockIloNumVar)),
      false,
      0,
    ),
    Arguments.of(
      "model with feasible solution populates variable values which are rounded correctly",
      LPVar("x", LPVarType.BOOLEAN, 0, 1),
      mock<IloCplex> {
        on { status } doReturn IloCplex.Status.Optimal
        on { getValue(mockIloNumVar) } doReturn 0.9
      },
      mutableMapOf(Pair("x", mockIloNumVar)),
      true,
      1,
    ),
    Arguments.of(
      "model with feasible solution and double variable types are populated as is",
      LPVar("x", LPVarType.DOUBLE, 0, 9.3),
      mock<IloCplex> {
        on { status } doReturn IloCplex.Status.Optimal
        on { getValue(mockIloNumVar) } doReturn 7.9
      },
      mutableMapOf(Pair("x", mockIloNumVar)),
      true,
      7.9,
    ),
    Arguments.of(
      "Exception while extracting results end with an error result in general",
      LPVar("x", LPVarType.BOOLEAN, 0, 1),
      mock<IloCplex> {
        on { status } doReturn IloCplex.Status.Optimal
        on { getValue(mockIloNumVar) } doThrow IloException()
      },
      mutableMapOf(Pair("x", mockIloNumVar)),
      false,
      0,
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForTestExtractResult")
  fun testExtractResult(
    testCase: String,
    lpVar: LPVar,
    cplexMock: IloCplex?,
    variableMap: MutableMap<String, IloNumVar>,
    wantResult: Boolean,
    want: Number,
  ) {
    log.info { "Test Case: $testCase" }
    val lpModel = LPModel()
    lpModel.variables.add(lpVar)
    val solver = CplexLpSolver(lpModel)
    // setup mocks
    setCplexModel(solver, cplexMock)
    setVariableMap(solver, variableMap)
    solver.solve()
    assertEquals(wantResult, lpVar.resultSet, "lpVar.resultSet")
    if (wantResult) {
      assertEquals(want, lpVar.result, "lpVar.result")
    }
  }

  private fun argsForTestInitVars() = Stream.of(
    Arguments.of(
      "Exception on variable initialization results is handled appropriately",
      null, LPVar("x", LPVarType.BOOLEAN),
      mock<IloCplex> {
        on { numVar(0.0, 1.0, IloNumVarType.Bool, "x") } doThrow IloException()
      },
      false,
      mutableMapOf<String, IloNumVar>(),
    ),
    Arguments.of(
      "Null cplex var on initialization is handled appropriately",
      null, LPVar("x", LPVarType.BOOLEAN),
      mock<IloCplex> {
        on { numVar(0.0, 1.0, IloNumVarType.Bool, "x") } doReturn null
      },
      false,
      mutableMapOf<String, IloNumVar>(),
    ),
    Arguments.of(
      "Boolean variable in default group and default bounds is initialized correctly",
      null, LPVar("x", LPVarType.BOOLEAN),
      mock<IloCplex> {
        on { numVar(0.0, 1.0, IloNumVarType.Bool, "x") } doReturn mockIloNumVar
      },
      true,
      mutableMapOf(Pair("x", mockIloNumVar))
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForTestInitVars")
  fun testInitVars(
    testCase: String,
    varGroup: String?,
    lpVar: LPVar,
    cplexMock: IloCplex?,
    wantSuccess: Boolean,
    wantVarMap: MutableMap<String, IloNumVar>
  ) {
    log.info { "Test Case: $testCase" }
    val lpModel = LPModel()
    if (varGroup == null) {
      lpModel.variables.add(lpVar)
    } else {
      lpModel.variables.add(varGroup, lpVar)
    }
    val solver = CplexLpSolver(lpModel)
    // setup mocks
    setCplexModel(solver, cplexMock)
    val gotVarMap = mutableMapOf<String, IloNumVar>().apply {
      setVariableMap(solver, this)
    }
    val gotSuccess = solver.initVars()
    assertEquals(wantSuccess, gotSuccess, "CplexLpSolver.initVars()")
    if (wantSuccess) {
      assertEquals(wantVarMap, gotVarMap, "CplexLpSolver.variableMap")
    }
  }

  private fun argsForTestInitConstraints() = Stream.of(
    Arguments.of(
      "Irreducible constraint results in an error",
      mutableMapOf<LPVar, IloNumVar>(),
      mock<IloCplex> {
        on { linearNumExpr() } doThrow IloException()
      },
      LPExpression().addTerm(2, "x"),
      LPOperator.LESS_EQUAL,
      LPExpression().add(3),
      false,
      mapOf<String, IloConstraint>(),
    ),
    Arguments.of(
      "Cplex linearNumExpr initialization exception results in a false value",
      mutableMapOf(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        on { linearNumExpr() } doThrow IloException()
      },
      LPExpression().addTerm(2, "x"),
      LPOperator.LESS_EQUAL,
      LPExpression().add(3),
      false,
      mapOf<String, IloConstraint>(),
    ),
    Arguments.of(
      "Cplex linearNumExpr initialization (null value) results in a false value",
      mutableMapOf(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        on { linearNumExpr() } doReturn null
      },
      LPExpression().addTerm(2, "x"),
      LPOperator.LESS_EQUAL,
      LPExpression().add(3),
      false,
      mapOf<String, IloConstraint>(),
    ),
    Arguments.of(
      "LHS initialization failure results in a false value",
      mutableMapOf(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        val numExpr = mock<IloLinearNumExpr> {
          on { addTerm(2.0, mockIloNumVar) } doThrow IloException()
        }
        on { linearNumExpr() } doReturn numExpr
      },
      LPExpression().addTerm(2, "x"),
      LPOperator.LESS_EQUAL,
      LPExpression().add(3),
      false,
      mapOf<String, IloConstraint>(),
    ),
    Arguments.of(
      "RHS initialization failure results in a false value",
      mutableMapOf(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        val numExpr = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doThrow IloException()
        }
        on { linearNumExpr() } doReturn numExpr
      },
      LPExpression().addTerm(2, "x"),
      LPOperator.LESS_EQUAL,
      LPExpression().add(3),
      false,
      mapOf<String, IloConstraint>(),
    ),
    Arguments.of(
      "Successful initialization of <= constraint",
      mutableMapOf(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        val lhs = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doThrow IloException()
          on { addTerm(2.0, mockIloNumVar) } doAnswer {}
        }
        val rhs = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doAnswer {}
          on { addTerm(2.0, mockIloNumVar) } doThrow IloException()
        }

        val expr = mutableListOf(lhs, rhs)
        on { linearNumExpr() } doAnswer { expr.removeAt(0) }
        on { addGe(lhs, rhs, "test") } doThrow IloException()
        on { addEq(lhs, rhs, "test") } doThrow IloException()
        on { addLe(lhs, rhs, "test") } doReturn mockConstraint
      },
      LPExpression().addTerm(2, "x"),
      LPOperator.LESS_EQUAL,
      LPExpression().add(3),
      true,
      mapOf(Pair("test", mockConstraint)),
    ),
    Arguments.of(
      "Successful initialization of >= constraint",
      mutableMapOf(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        val lhs = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doThrow IloException()
          on { addTerm(2.0, mockIloNumVar) } doAnswer {}
        }
        val rhs = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doAnswer {}
          on { addTerm(2.0, mockIloNumVar) } doThrow IloException()
        }

        val expr = mutableListOf(lhs, rhs)
        on { linearNumExpr() } doAnswer { expr.removeAt(0) }
        on { addGe(lhs, rhs, "test") } doReturn mockConstraint
        on { addEq(lhs, rhs, "test") } doThrow IloException()
        on { addLe(lhs, rhs, "test") } doThrow IloException()
      },
      LPExpression().addTerm(2, "x"),
      LPOperator.GREATER_EQUAL,
      LPExpression().add(3),
      true,
      mapOf(Pair("test", mockConstraint)),
    ),
    Arguments.of(
      "Successful initialization of = constraint",
      mutableMapOf(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        val lhs = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doThrow IloException()
          on { addTerm(2.0, mockIloNumVar) } doAnswer {}
        }
        val rhs = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doAnswer {}
          on { addTerm(2.0, mockIloNumVar) } doThrow IloException()
        }

        val expr = mutableListOf(lhs, rhs)
        on { linearNumExpr() } doAnswer { expr.removeAt(0) }
        on { addGe(lhs, rhs, "test") } doThrow IloException()
        on { addEq(lhs, rhs, "test") } doReturn mockConstraint
        on { addLe(lhs, rhs, "test") } doThrow IloException()
      },
      LPExpression().addTerm(2, "x"),
      LPOperator.EQUAL,
      LPExpression().add(3),
      true,
      mapOf(Pair("test", mockConstraint)),
    ),
    Arguments.of(
      "Nil return results in an error",
      mutableMapOf(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        val lhs = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doThrow IloException()
          on { addTerm(2.0, mockIloNumVar) } doAnswer {}
        }
        val rhs = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doAnswer {}
          on { addTerm(2.0, mockIloNumVar) } doThrow IloException()
        }
        val expr = mutableListOf(lhs, rhs)
        on { linearNumExpr() } doAnswer { expr.removeAt(0) }
        on { addEq(lhs, rhs, "test") } doReturn null
      },
      LPExpression().addTerm(2, "x"),
      LPOperator.EQUAL,
      LPExpression().add(3),
      false,
      mapOf<String, IloConstraint>(),
    ),
    Arguments.of(
      "Exception return results in an error",
      mutableMapOf(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        val lhs = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doThrow IloException()
          on { addTerm(2.0, mockIloNumVar) } doAnswer {}
        }
        val rhs = mock<IloLinearNumExpr> {
          on { constant = 3.0 } doAnswer {}
          on { addTerm(2.0, mockIloNumVar) } doThrow IloException()
        }
        val expr = mutableListOf(lhs, rhs)
        on { linearNumExpr() } doAnswer { expr.removeAt(0) }
        on { addEq(lhs, rhs, "test") } doThrow IloException()
      },
      LPExpression().addTerm(2, "x"),
      LPOperator.EQUAL,
      LPExpression().add(3),
      false,
      mapOf<String, IloConstraint>(),
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForTestInitConstraints")
  fun testInitConstraints(
    desc: String,
    variableMap: MutableMap<LPVar, IloNumVar>,
    cplexMock: IloCplex?,
    lhs: LPExpression,
    operator: LPOperator,
    rhs: LPExpression,
    wantSuccess: Boolean,
    wantConstraintMap: Map<String, IloConstraint>
  ) {
    log.info { "Test Case : $desc" }
    val lpModel = LPModel("testModel")
    val cplexVarMap = mutableMapOf<String, IloNumVar>()
    val cplexConstraintMap = mutableMapOf<String, IloConstraint>()
    variableMap.forEach { entry ->
      run {
        lpModel.variables.add(entry.key)
        cplexVarMap[entry.key.identifier] = entry.value
      }
    }
    val solver = CplexLpSolver(lpModel)
    // setup mocks
    setCplexModel(solver, cplexMock)
    setVariableMap(solver, cplexVarMap)
    setConstraintMap(solver, cplexConstraintMap)

    lpModel.constraints.add(LPConstraint("test", lhs, operator, rhs))

    val gotSuccess = solver.initConstraints()
    assertEquals(wantSuccess, gotSuccess, "CplexLpSolver.initConstraints()")
    if (wantSuccess) {
      assertEquals(wantConstraintMap, cplexConstraintMap, "CplexLpSolver.constraintMap")
    }
  }

  private fun argsForTestInitObjective() = Stream.of(
    Arguments.of(
      "Irreducible objective results in an error",
      mutableMapOf<LPVar, IloNumVar>(),
      mock<IloCplex> {
        on { linearNumExpr() } doReturn mockExpression
      },
      LPExpression().addTerm(2, "x"),
      LPObjectiveType.MINIMIZE,
      false,
      null,
    ),
    Arguments.of(
      "Exception when generating constraints results in an error",
      mutableMapOf<LPVar, IloNumVar>(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        on { linearNumExpr() } doThrow IloException()
      },
      LPExpression().addTerm(2, "x"),
      LPObjectiveType.MINIMIZE,
      false,
      null,
    ),
    Arguments.of(
      "Exception when setting direction results in an error",
      mutableMapOf<LPVar, IloNumVar>(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        on { linearNumExpr() } doReturn mockExpression
        on { addMinimize(mockExpression) } doThrow IloException()
      },
      LPExpression().addTerm(2, "x"),
      LPObjectiveType.MINIMIZE,
      false,
      null,
    ),
    Arguments.of(
      "Minimize is initialized correctly",
      mutableMapOf<LPVar, IloNumVar>(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        on { linearNumExpr() } doReturn mockExpression
        on { addMaximize(mockExpression) } doThrow IloException()
        on { addMinimize(mockExpression) } doReturn mock {}
      },
      LPExpression().addTerm(2, "x"),
      LPObjectiveType.MINIMIZE,
      true,
      mockExpression,
    ),
    Arguments.of(
      "Maximize is initialized correctly",
      mutableMapOf<LPVar, IloNumVar>(Pair(LPVar("x", LPVarType.BOOLEAN), mockIloNumVar)),
      mock<IloCplex> {
        on { linearNumExpr() } doReturn mockExpression
        on { addMinimize(mockExpression) } doThrow IloException()
        on { addMaximize(mockExpression) } doReturn mock {}
      },
      LPExpression().addTerm(2, "x"),
      LPObjectiveType.MAXIMIZE,
      true,
      mockExpression,
    ),
  )
  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForTestInitObjective")
  fun testInitObjective(
    desc: String,
    variableMap: MutableMap<LPVar, IloNumVar>,
    cplexMock: IloCplex?,
    objectiveExpr: LPExpression,
    objectiveType: LPObjectiveType,
    wantSuccess: Boolean,
    wantCplexExpr: IloLinearNumExpr?
  ) {
    log.info { "Test Case : $desc" }
    val lpModel = LPModel("testModel")
    val cplexVarMap = mutableMapOf<String, IloNumVar>()
    variableMap.forEach { entry ->
      run {
        lpModel.variables.add(entry.key)
        cplexVarMap[entry.key.identifier] = entry.value
      }
    }
    val solver = CplexLpSolver(lpModel)
    // setup mocks
    setCplexModel(solver, cplexMock)
    setVariableMap(solver, cplexVarMap)
    lpModel.objective.expression = objectiveExpr
    lpModel.objective.objective = objectiveType
    val gotSuccess = solver.initObjectiveFunction()
    assertEquals(wantSuccess, gotSuccess, "CplexLpSolver.initObjectiveFunction()")
    if (wantSuccess) {
      assertEquals(wantCplexExpr, getObjective(solver), "CplexLpSolver.cplexObjective")
    }
  }
}