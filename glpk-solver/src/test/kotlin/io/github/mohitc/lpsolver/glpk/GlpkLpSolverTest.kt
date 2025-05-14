package io.github.mohitc.lpsolver.glpk

import io.github.mohitc.glpk.ffm.GLPKBoundType
import io.github.mohitc.glpk.ffm.GLPKMipStatus
import io.github.mohitc.glpk.ffm.GLPKObjective
import io.github.mohitc.glpk.ffm.GLPKProblem
import io.github.mohitc.glpk.ffm.GLPKVarKind
import io.github.mohitc.glpk.ffm.GlpIocp
import io.github.mohitc.lpapi.model.LPConstraint
import io.github.mohitc.lpapi.model.LPExpression
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPVar
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import java.lang.RuntimeException
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GlpkLpSolverTest {
  private val log = KotlinLogging.logger { this.javaClass.name }

  companion object {
    val mockCfg = GlpIocp()
    var columIndex: Int = 1
    var rowIndex: Int = 1

    fun resetIndexCounters() {
      columIndex = 1
      rowIndex = 1
    }
  }

  private fun setParameter(
    solver: GlpkLpSolver,
    field: String,
    value: Any?,
  ) {
    solver.javaClass.getDeclaredField(field).let {
      it.isAccessible = true
      it.set(solver, value)
    }
  }

  private fun setVariableMap(
    solver: GlpkLpSolver,
    variableMap: MutableMap<String, Int>,
  ) = setParameter(solver, "variableMap", variableMap)

  private fun setConstraintMap(
    solver: GlpkLpSolver,
    variableMap: MutableMap<String, Int>,
  ) = setParameter(solver, "constraintMap", variableMap)

  private fun setModel(
    solver: GlpkLpSolver,
    model: GLPKProblem,
  ) = setParameter(solver, "glpkModel", model)

  private fun setCfg(
    solver: GlpkLpSolver,
    cfg: GlpIocp,
  ) = setParameter(solver, "cfg", cfg)

  private fun argsForInitModel() =
    Stream.of(
      Arguments.of(
        "Exception in glp_set_prob_name() results in a failure",
        mock<GLPKProblem> {
          on { setModelName("test-model") }.thenThrow(RuntimeException())
        },
        LPModel("test-model"),
        false,
        null,
      ),
      Arguments.of(
        "Model is initialized correctly",
        mock<GLPKProblem> {},
        LPModel("test-model"),
        true,
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitModel")
  fun testInitModel(
    desc: String,
    glpkModel: GLPKProblem,
    model: LPModel,
    wantSuccess: Boolean,
  ) {
    log.info { "Test Case: $desc" }
    val solver = GlpkLpSolver(model)
    setModel(solver, glpkModel)
    val gotSuccess = solver.initModel()
    assertEquals(wantSuccess, gotSuccess, "solver.initModel()")
  }

  private fun argsForInitVars() =
    Stream.of(
      Arguments.of(
        "Exception in addCols() results in a failure",
        mock<GLPKProblem> {
          on { addCols(1) }.thenThrow(RuntimeException())
        },
        LPVar("x", LPVarType.BOOLEAN),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Exception in setColName() results in a failure",
        mock<GLPKProblem> {
          on { addCols(1) }.then {
            columIndex++
            columIndex - 1
          }
          on { setColName(1, "x") }.thenThrow(RuntimeException())
        },
        LPVar("x", LPVarType.BOOLEAN),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Exception in setColKind() results in a failure",
        mock<GLPKProblem> {
          on { addCols(1) }.then {
            columIndex++
            columIndex - 1
          }
          on { setColKind(1, GLPKVarKind.BOOLEAN) }
            .thenThrow(RuntimeException())
        },
        LPVar("x", LPVarType.BOOLEAN),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Exception in setColBounds() results in a failure",
        mock<GLPKProblem> {
          on { addCols(1) }.then {
            columIndex++
            columIndex - 1
          }
          on {
            setColBounds(
              1,
              GLPKBoundType.DOUBLE_BOUNDED,
              0.0,
              1.0,
            )
          }.thenThrow(RuntimeException())
        },
        LPVar("x", LPVarType.BOOLEAN, 0, 1),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Exception in setColBounds() results in a failure (check for fixed value bound type)",
        mock<GLPKProblem> {
          on { addCols(1) }.then {
            columIndex++
            columIndex - 1
          }
          on {
            setColBounds(
              1,
              GLPKBoundType.FIXED,
              1.0,
              1.0,
            )
          }.thenThrow(RuntimeException())
        },
        LPVar("x", LPVarType.BOOLEAN, 1, 1),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Success Case",
        mock<GLPKProblem> {
          on { addCols(1) }.then {
            columIndex++
            columIndex - 1
          }
        },
        LPVar("x", LPVarType.BOOLEAN),
        true,
        mutableMapOf(Pair("x", 1)),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitVars")
  fun testInitVars(
    desc: String,
    glpkModel: GLPKProblem,
    lpVar: LPVar,
    wantSuccess: Boolean,
    wantVarMap: Map<String, Int>,
  ) {
    log.info { "Test Case $desc" }
    resetIndexCounters()
    val lpModel = LPModel("test")
    lpModel.variables.add(lpVar)
    val solver = GlpkLpSolver(lpModel)
    setModel(solver, glpkModel)
    solver.initModel()
    val gotVarMap =
      mutableMapOf<String, Int>().apply {
        setVariableMap(solver, this)
      }
    val gotSuccess = solver.initVars()
    assertEquals(wantSuccess, gotSuccess, "solver.initVars()")
    assertEquals(gotVarMap, wantVarMap, "solver.variableMap")
  }

  private fun argsForGetSolutionStatus() =
    Stream.of(
      Arguments.of(
        "Undefined goes to LPSolutionStatus Unknown",
        GLPKMipStatus.UNDEFINED,
        LPSolutionStatus.UNKNOWN,
      ),
      Arguments.of(
        "Optimal goes to LPSolutionStatus Optimal",
        GLPKMipStatus.OPTIMAL,
        LPSolutionStatus.OPTIMAL,
      ),
      Arguments.of(
        "Feasible goes to LPSolutionStatus Time limited",
        GLPKMipStatus.FEASIBLE,
        LPSolutionStatus.TIME_LIMIT,
      ),
      Arguments.of(
        "Infeasible goes to LPSolutionStatus Infeasible",
        GLPKMipStatus.NOFEASIBLE,
        LPSolutionStatus.INFEASIBLE,
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForGetSolutionStatus")
  fun testGetSolutionStatus(
    desc: String,
    glpkStatus: GLPKMipStatus,
    wantStatus: LPSolutionStatus,
  ) {
    log.info { "Test Case: $desc" }
    val model = LPModel()
    val solver = GlpkLpSolver(model)
    assertEquals(wantStatus, solver.getSolutionStatus(glpkStatus), "solver.getSolutionStatus($glpkStatus)")
  }

  private fun argsForInitObjectiveFunction() =
    Stream.of(
      Arguments.of(
        "setObjective() results in an error",
        mock<GLPKProblem> {
          on { setObjective(GLPKObjective.MINIMIZE) }
            .thenThrow(RuntimeException())
        },
        LPModel("test"),
        mutableMapOf<String, Int>(),
        LPObjectiveType.MINIMIZE,
        io.github.mohitc.lpapi.model
          .LPExpression(),
        false,
      ),
      Arguments.of(
        "setObjective() results in an error (Maximize)",
        mock<GLPKProblem> {
          on { setObjective(GLPKObjective.MAXIMIZE) }
            .thenThrow(RuntimeException())
        },
        LPModel("test"),
        mutableMapOf<String, Int>(),
        LPObjectiveType.MAXIMIZE,
        io.github.mohitc.lpapi.model
          .LPExpression(),
        false,
      ),
      Arguments.of(
        "Failure to reduce objective results in a false value",
        mock<GLPKProblem> {
          on { addCols(1) }.then {
            columIndex++
            columIndex - 1
          }
        },
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
        },
        mutableMapOf(Pair("x", 1), Pair("y", 2)),
        LPObjectiveType.MINIMIZE,
        io.github.mohitc.lpapi.model
          .LPExpression()
          .apply { this.addTerm("a", "x").addTerm(3, "y") },
        false,
      ),
      Arguments.of(
        "setObjectiveCoefficient throws runtime exception on setting constant",
        mock<GLPKProblem> {
          on { addCols(1) }.then {
            columIndex++
            columIndex - 1
          }
          on { setObjectiveCoefficient(0, 3.0) }.thenThrow(RuntimeException())
        },
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
        },
        mutableMapOf(Pair("x", 1)),
        LPObjectiveType.MAXIMIZE,
        io.github.mohitc.lpapi.model
          .LPExpression()
          .apply { this.add(3).addTerm(2, "x") },
        false,
      ),
      Arguments.of(
        "setObjectiveCoefficient throws runtime exception on setting variable coefficient",
        mock<GLPKProblem> {
          on { addCols(1) }.then {
            columIndex++
            columIndex - 1
          }
          on { setObjectiveCoefficient(1, 2.0) }.thenThrow(RuntimeException())
        },
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
        },
        mutableMapOf(Pair("x", 1)),
        LPObjectiveType.MAXIMIZE,
        io.github.mohitc.lpapi.model
          .LPExpression()
          .apply { this.add(3).addTerm(2, "x") },
        false,
      ),
      Arguments.of(
        "Objective is configured successfully",
        mock<GLPKProblem> {
          on { addCols(1) }.then {
            columIndex++
            columIndex - 1
          }
        },
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
        },
        mutableMapOf(Pair("x", 1)),
        LPObjectiveType.MINIMIZE,
        io.github.mohitc.lpapi.model
          .LPExpression()
          .apply { this.add(3).addTerm(2, "x") },
        true,
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitObjectiveFunction")
  fun testInitObjectiveFunction(
    desc: String,
    glpkModel: GLPKProblem,
    model: LPModel,
    varMap: MutableMap<String, Int>,
    objective: LPObjectiveType,
    expr: io.github.mohitc.lpapi.model.LPExpression,
    wantSuccess: Boolean,
  ) {
    log.info { "Test Case: $desc" }
    resetIndexCounters()
    val solver = GlpkLpSolver(model)
    setModel(solver, glpkModel)
    model.objective.expression = expr
    model.objective.objective = objective
    setVariableMap(solver, varMap)
    solver.initModel()
    val gotSuccess = solver.initObjectiveFunction()
    assertEquals(wantSuccess, gotSuccess, "solver.initObjectiveFunction()")
  }

  private fun argsForSolve() =
    Stream.of(
      Arguments.of(
        "Exception on intopt() results in an errored status",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1)))
            setCfg(this, mockCfg)
            this.initModel()
            setModel(
              this,
              mock<GLPKProblem> {
                on { intopt(mockCfg) }.thenThrow(RuntimeException())
              },
            )
          },
        LPSolutionStatus.ERROR,
        mutableMapOf<String, Number>(),
      ),
      Arguments.of(
        "Undefined results in a solution status of unknown",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1)))
            this.initModel()
            setModel(
              this,
              mock<GLPKProblem> {
                on { mipStatus() }.thenReturn(GLPKMipStatus.UNDEFINED)
              },
            )
          },
        LPSolutionStatus.UNKNOWN,
        mutableMapOf<String, Number>(),
      ),
      Arguments.of(
        "Error in result extraction results in an errored state",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1)))
            this.initModel()
            setModel(
              this,
              mock<GLPKProblem> {
                on { mipStatus() }.thenReturn(GLPKMipStatus.OPTIMAL)
                on { mipColVal(1) }.thenThrow(RuntimeException())
              },
            )
          },
        LPSolutionStatus.ERROR,
        mutableMapOf<String, Number>(),
      ),
      Arguments.of(
        "Known solution results in the population of the results",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1)))
            this.initModel()
            setModel(
              this,
              mock<GLPKProblem> {
                on { mipStatus() }.thenReturn(GLPKMipStatus.OPTIMAL)
                on { mipColVal(1) }.thenReturn(3.2)
              },
            )
          },
        LPSolutionStatus.OPTIMAL,
        mutableMapOf<String, Number>(Pair("x", 3)),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForSolve")
  fun testSolve(
    desc: String,
    model: LPModel,
    initSolver: (LPModel) -> GlpkLpSolver,
    wantStatus: LPSolutionStatus,
    wantResults: MutableMap<String, Number>,
  ) {
    log.info { "Test Case: $desc" }
    val solver = initSolver(model)
    val gotStatus = solver.solve()
    assertEquals(wantStatus, gotStatus, "solver.solve()")
    val gotResults =
      model.variables
        .allValues()
        .filter { it.resultSet }
        .associate { Pair(it.identifier, it.result) }
    assertEquals(wantResults, gotResults, "model.results")
  }

  private fun argsForInitConstraints() =
    Stream.of(
      Arguments.of(
        "Failure to reduce a constraint results in false",
        mock<GLPKProblem> {},
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
          this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              this.lhs.addTerm("a", "x").addTerm("b", "y")
              this.operator = LPOperator.LESS_EQUAL
              this.rhs.add(10)
            },
          )
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
            this.initModel()
          },
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Assert that constraint row name is set correctly",
        mock<GLPKProblem> {
          on { addRows(1) }.then {
            rowIndex++
            rowIndex - 1
          }
          on { setRowName(1, "test-constraint") }
            .thenThrow(RuntimeException())
        },
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
          this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              this.lhs.addTerm("x").addTerm("y")
              this.operator = LPOperator.LESS_EQUAL
              this.rhs.add(10)
            },
          )
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
            this.initModel()
          },
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "LessEqual results in a call to set row bounds as GLPKConstants.GLP_UP",
        mock<GLPKProblem> {
          on { addRows(1) }.then {
            rowIndex++
            rowIndex - 1
          }
          on {
            setRowBounds(
              1,
              GLPKBoundType.UPPER_BOUNDED,
              0.0,
              10.0,
            )
          }.thenThrow(RuntimeException())
        },
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
          this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              this.lhs.addTerm("x").addTerm(-2, "y")
              this.operator = LPOperator.LESS_EQUAL
              this.rhs.add(10)
            },
          )
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
            this.initModel()
          },
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "GreaterEqual results in a call to set row bounds as GLPKConstants.GLP_LO",
        mock<GLPKProblem> {
          on { addRows(1) }.then {
            rowIndex++
            rowIndex - 1
          }
          on {
            setRowBounds(
              1,
              GLPKBoundType.LOWER_BOUNDED,
              10.0,
              0.0,
            )
          }.thenThrow(RuntimeException())
        },
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
          this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              this.lhs.addTerm("x").addTerm(-2, "y")
              this.operator = LPOperator.GREATER_EQUAL
              this.rhs.add(10)
            },
          )
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
            this.initModel()
          },
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Equals results in a call to set row bounds as GLPKConstants.GLP_FX",
        mock<GLPKProblem> {
          on { addRows(1) }.then {
            rowIndex++
            rowIndex - 1
          }
          on {
            setRowBounds(
              1,
              GLPKBoundType.FIXED,
              10.0,
              10.0,
            )
          }.thenThrow(RuntimeException())
        },
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
          this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              this.lhs.addTerm("x").addTerm(-2, "y")
              this.operator = LPOperator.EQUAL
              this.rhs.add(10)
            },
          )
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
            this.initModel()
          },
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Assert that row is configured correctly",
        mock<GLPKProblem> {
          on { addRows(1) }.then {
            rowIndex++
            rowIndex - 1
          }
          on {
            setMatrixRow(
              1,
              2,
              listOf(1, 2),
              listOf(1.0, -2.0),
            )
          }.thenThrow(RuntimeException())
        },
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
          this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              this.lhs
                .addTerm("x")
                .addTerm(-2, "y")
                .add(10)
              this.operator = LPOperator.LESS_EQUAL
            },
          )
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
            this.initModel()
          },
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Success case",
        mock<GLPKProblem> {
          on { addRows(1) }.then {
            rowIndex++
            rowIndex - 1
          }
        },
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
          this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
          this.constraints.add(
            LPConstraint("test-constraint").apply {
              this.lhs
                .addTerm("x")
                .addTerm(-2, "y")
                .add(10)
              this.operator = LPOperator.EQUAL
            },
          )
        },
        fun(model: LPModel): GlpkLpSolver =
          GlpkLpSolver(model).apply {
            setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
            this.initModel()
          },
        true,
        mutableMapOf(Pair("test-constraint", 1)),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitConstraints")
  fun testInitConstraints(
    desc: String,
    glpkModel: GLPKProblem,
    model: LPModel,
    initSolver: (LPModel) -> GlpkLpSolver,
    wantSuccess: Boolean,
    wantConstraintMap: MutableMap<String, Int>,
  ) {
    log.info { "Test Case : $desc" }
    val solver = initSolver(model)
    setModel(solver, glpkModel)
    resetIndexCounters()
    val gotConstraintMap =
      mutableMapOf<String, Int>().apply {
        setConstraintMap(solver, this)
      }
    val gotSuccess = solver.initConstraints()
    assertEquals(wantSuccess, gotSuccess, "solver.initConstraints()")
    assertEquals(gotConstraintMap, wantConstraintMap, "solver.constraintMap")
  }
}