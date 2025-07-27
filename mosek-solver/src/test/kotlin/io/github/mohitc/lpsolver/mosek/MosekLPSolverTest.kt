package io.github.mohitc.lpsolver.mosek

import com.mosek.mosek.Task
import com.mosek.mosek.boundkey
import com.mosek.mosek.objsense
import com.mosek.mosek.solsta
import com.mosek.mosek.soltype
import com.mosek.mosek.variabletype
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MosekLPSolverTest {
  private val log = KotlinLogging.logger { this.javaClass.name }

  private fun setParameter(
    solver: MosekLPSolver,
    field: String,
    value: Any?,
  ) {
    solver.javaClass.getDeclaredField(field).let {
      it.isAccessible = true
      it.set(solver, value)
    }
  }

  private fun setModel(
    solver: MosekLPSolver,
    model: Task?,
  ) = setParameter(solver, "baseModel", model)

  private fun setVariableMap(
    solver: MosekLPSolver,
    variableMap: MutableMap<String, Int>,
  ) = setParameter(solver, "variableMap", variableMap)

  private fun setConstraintMap(
    solver: MosekLPSolver,
    constraintMap: MutableMap<String, Int>,
  ) = setParameter(solver, "constraintMap", constraintMap)

  private fun argsForInitVars() =
    Stream.of(
      Arguments.of(
        "Exception while initializing variable columns",
        mock<Task> {
          on { appendvars(1) }.thenThrow(RuntimeException("Error"))
        },
        LPVar("X", LPVarType.BOOLEAN),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Exception while initializing variable name",
        mock<Task> {
          on { putvarname(0, "X") }.thenThrow(RuntimeException("Error"))
        },
        LPVar("X", LPVarType.BOOLEAN),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Exception while initializing variable bounds",
        mock<Task> {
          on { putvarbound(0, boundkey.ra, 0.0, 1.0) }.thenThrow(RuntimeException("Error"))
        },
        LPVar("X", LPVarType.BOOLEAN),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Exception while initializing variable type",
        mock<Task> {
          on { putvartype(0, variabletype.type_int) }.thenThrow(RuntimeException("Error"))
        },
        LPVar("X", LPVarType.BOOLEAN),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Correctly Initialize a continuous variable",
        mock<Task> {
          on { putvartype(0, variabletype.type_int) }.thenThrow(RuntimeException("Error"))
        },
        LPVar("X", LPVarType.DOUBLE, 1.0, 3.0),
        true,
        mutableMapOf(Pair("X", 0)),
      ),
      Arguments.of(
        "Correctly Initialize an integer variable",
        mock<Task> {
          on { putvartype(0, variabletype.type_cont) }.thenThrow(RuntimeException("Error"))
        },
        LPVar("X", LPVarType.INTEGER, 1.0, 3.0),
        true,
        mutableMapOf(Pair("X", 0)),
      ),
      Arguments.of(
        "Correctly Initialize a boolean variable",
        mock<Task> {
          on { putvartype(0, variabletype.type_cont) }.thenThrow(RuntimeException("Error"))
        },
        LPVar("X", LPVarType.INTEGER, 1.0, 3.0),
        true,
        mutableMapOf(Pair("X", 0)),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitVars")
  fun testInitVars(
    desc: String,
    mosekModel: Task,
    lpVar: LPVar,
    wantSuccess: Boolean,
    wantVarMap: Map<String, Int>,
  ) {
    log.info { "Test case: $desc" }
    val lpModel = LPModel("test").apply { this.variables.add(lpVar) }
    val solver = MosekLPSolver(lpModel)
    setModel(solver, mosekModel)
    val gotVarMap =
      mutableMapOf<String, Int>().apply {
        setVariableMap(solver, this)
      }
    val gotSuccess = solver.initVars()
    assertEquals(wantSuccess, gotSuccess, "solver.initVars() want $wantSuccess got $gotSuccess")
    assertEquals(wantVarMap, gotVarMap, "VariableMap want=$wantVarMap, got=$gotVarMap")
  }

  private fun argsForInitConstraints() =
    Stream.of(
      Arguments.of(
        "Error while initializing constraints",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint(
              "x+y=1",
              LPExpression().addTerm("x").addTerm("y"),
              LPOperator.EQUAL,
              LPExpression().add(1),
            ),
          )
        },
        mock<Task> {
          on { appendcons(1) }.thenThrow(RuntimeException("error"))
        },
        mutableMapOf(Pair("x", 0), Pair("y", 1)),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Error while applying equality",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint(
              "x+y=1",
              LPExpression().addTerm("x").addTerm("y"),
              LPOperator.EQUAL,
              LPExpression().add(1),
            ),
          )
        },
        mock<Task> {
          on { putconbound(0, boundkey.fx, 1.0, 1.0) }.thenThrow(RuntimeException("error"))
        },
        mutableMapOf(Pair("x", 0), Pair("y", 1)),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Error while applying <= operator",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint(
              "x+y<=1",
              LPExpression().addTerm("x").addTerm("y"),
              LPOperator.LESS_EQUAL,
              LPExpression().add(1),
            ),
          )
        },
        mock<Task> {
          on { putconbound(0, boundkey.up, 0.0, 1.0) }.thenThrow(RuntimeException("error"))
        },
        mutableMapOf(Pair("x", 0), Pair("y", 1)),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Error while applying >= operator",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint(
              "x+y>=1",
              LPExpression().addTerm("x").addTerm("y"),
              LPOperator.GREATER_EQUAL,
              LPExpression().add(1),
            ),
          )
        },
        mock<Task> {
          on { putconbound(0, boundkey.lo, 1.0, 0.0) }.thenThrow(RuntimeException("error"))
        },
        mutableMapOf(Pair("x", 0), Pair("y", 1)),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Error while initializing var contributions (x)",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint(
              "2x-y>=1",
              LPExpression().addTerm(2.0, "x").addTerm(-1.0, "y"),
              LPOperator.GREATER_EQUAL,
              LPExpression().add(1),
            ),
          )
        },
        mock<Task> {
          on { putacol(0, IntArray(1) { 0 }, DoubleArray(1) { 2.0 }) }.thenThrow(RuntimeException("error"))
        },
        mutableMapOf(Pair("x", 0), Pair("y", 1)),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Error while initializing var contributions (y)",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint(
              "2x-y>=1",
              LPExpression().addTerm("x").addTerm("x").addTerm(-1.0, "y"),
              LPOperator.GREATER_EQUAL,
              LPExpression().add(1),
            ),
          )
        },
        mock<Task> {
          on { putacol(1, IntArray(1) { 0 }, DoubleArray(1) { -1.0 }) }.thenThrow(RuntimeException("error"))
        },
        mutableMapOf(Pair("x", 0), Pair("y", 1)),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Success Initializing Constraints",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.constraints.add(
            LPConstraint(
              "2x-y>=1",
              LPExpression().addTerm("x").addTerm("x").addTerm(-1.0, "y"),
              LPOperator.GREATER_EQUAL,
              LPExpression().add(1),
            ),
          )
        },
        mock<Task> {},
        mutableMapOf(Pair("x", 0), Pair("y", 1)),
        true,
        mutableMapOf(Pair("2x-y>=1", 0)),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitConstraints")
  fun testInitConstraints(
    desc: String,
    lpModel: LPModel,
    model: Task,
    variableMap: MutableMap<String, Int>,
    wantSuccess: Boolean,
    wantConstraintMap: Map<String, Int>,
  ) {
    log.info("Test Case: $desc")
    val solver = MosekLPSolver(lpModel)
    setModel(solver, model)
    setVariableMap(solver, variableMap)
    val gotConstraintMap =
      mutableMapOf<String, Int>().apply {
        setConstraintMap(solver, this)
      }
    val gotSuccess = solver.initConstraints()
    assertEquals(
      wantSuccess,
      gotSuccess,
      "initConstraints() want $wantSuccess got $gotSuccess",
    )
    if (wantSuccess) {
      assertEquals(
        wantConstraintMap,
        gotConstraintMap,
        "initConstraints() want $wantConstraintMap got $gotConstraintMap",
      )
    }
  }

  private fun argsForInitObjective() =
    Stream.of(
      Arguments.of(
        "Error while initializing the value of a specific variable",
        LPModel("MAX x+2y+3z+4").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.variables.add(LPVar("z", LPVarType.BOOLEAN))
          this.objective.objective = LPObjectiveType.MAXIMIZE
          this.objective.expression
            .addTerm("x")
            .addTerm("y")
            .addTerm("y")
            .addTerm(3, "z")
            .add(4)
        },
        mock<Task> {
          on { putcj(0, 1.0) }.thenThrow(RuntimeException("Error"))
        },
        mutableMapOf(
          Pair("x", 0),
          Pair("y", 1),
          Pair("z", 2),
        ),
        false,
      ),
      Arguments.of(
        "Error while initializing the value of a specific variable (y)",
        LPModel("MAX x+2y+3z+4").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.variables.add(LPVar("z", LPVarType.BOOLEAN))
          this.objective.objective = LPObjectiveType.MAXIMIZE
          this.objective.expression
            .addTerm("x")
            .addTerm("y")
            .addTerm("y")
            .addTerm(3, "z")
            .add(4)
        },
        mock<Task> {
          on { putcj(1, 2.0) }.thenThrow(RuntimeException("Error"))
        },
        mutableMapOf(
          Pair("x", 0),
          Pair("y", 1),
          Pair("z", 2),
        ),
        false,
      ),
      Arguments.of(
        "Error while configuring the objective sense",
        LPModel("MAX x+2y+3z+4").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.variables.add(LPVar("z", LPVarType.BOOLEAN))
          this.objective.objective = LPObjectiveType.MAXIMIZE
          this.objective.expression
            .addTerm("x")
            .addTerm("y")
            .addTerm("y")
            .addTerm(3, "z")
            .add(4)
        },
        mock<Task> {
          on { putobjsense(objsense.maximize) }.thenThrow(RuntimeException("Error"))
        },
        mutableMapOf(
          Pair("x", 0),
          Pair("y", 1),
          Pair("z", 2),
        ),
        false,
      ),
      Arguments.of(
        "Successful initialization",
        LPModel("MIN x+2y+3z+4").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
          this.variables.add(LPVar("z", LPVarType.BOOLEAN))
          this.objective.objective = LPObjectiveType.MINIMIZE
          this.objective.expression
            .addTerm("x")
            .addTerm("y")
            .addTerm("y")
            .addTerm(3, "z")
            .add(4)
        },
        mock<Task> {
          on { putobjsense(objsense.maximize) }.thenThrow(RuntimeException("Error"))
        },
        mutableMapOf(
          Pair("x", 0),
          Pair("y", 1),
          Pair("z", 2),
        ),
        true,
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitObjective")
  fun testInitObjective(
    desc: String,
    lpModel: LPModel,
    model: Task,
    variableMap: MutableMap<String, Int>,
    wantSuccess: Boolean,
  ) {
    log.info("Test Case: $desc")
    val solver = MosekLPSolver(lpModel)
    setModel(solver, model)
    setVariableMap(solver, variableMap)
    val gotSuccess = solver.initObjectiveFunction()
    assertEquals(wantSuccess, gotSuccess, "initObjectiveFunction() want $wantSuccess got $gotSuccess")
  }

  fun argsForSolve() =
    Stream.of(
      Arguments.of(
        "Error during optimize call",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.INTEGER, 0.0, 10.0))
          this.variables.add(LPVar("z", LPVarType.DOUBLE, 1.0, 2.0))
          this.objective.expression
            .addTerm("x")
            .addTerm(2, "y")
            .addTerm(3, "z")
            .add(4)
        },
        mock<Task> {
          on { optimize() }.thenThrow(RuntimeException("error"))
        },
        mutableMapOf(
          Pair("x", 0),
          Pair("y", 1),
          Pair("z", 2),
        ),
        LPSolutionStatus.ERROR,
        mutableMapOf<String, Number>(),
        0.0,
      ),
      Arguments.of(
        "Extract Results",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.INTEGER, 0.0, 10.0))
          this.variables.add(LPVar("z", LPVarType.DOUBLE, 1.0, 2.0))
          this.objective.expression
            .addTerm("x")
            .addTerm(2, "y")
            .addTerm(3, "z")
            .add(4)
        },
        mock<Task> {
          on { getsolsta(soltype.itg) }.thenReturn(solsta.integer_optimal)
          on { getxx(soltype.itg) }.thenReturn(arrayOf(1.0, 2.1, 1.5).toDoubleArray())
        },
        mutableMapOf(
          Pair("x", 0),
          Pair("y", 1),
          Pair("z", 2),
        ),
        LPSolutionStatus.OPTIMAL,
        mutableMapOf<String, Number>(
          Pair("x", 1),
          Pair("y", 2),
          Pair("z", 1.5),
        ),
        13.5,
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForSolve")
  fun testSolve(
    desc: String,
    lpModel: LPModel,
    model: Task,
    variableMap: MutableMap<String, Int>,
    wantStatus: LPSolutionStatus,
    wantVarResult: Map<String, Number>,
    wantObjectiveVal: Double,
  ) {
    log.info("Test Case: $desc")
    val solver = MosekLPSolver(lpModel)
    setModel(solver, model)
    setVariableMap(solver, variableMap)
    val gotStatus = solver.solve()
    assertEquals(wantStatus, gotStatus, "solve() want $wantStatus got $gotStatus")
    if (wantStatus == LPSolutionStatus.OPTIMAL) {
      lpModel.variables.allValues().forEach { lpvar ->
        assertTrue(lpvar.resultSet, "${lpvar.resultSet} want true got false")
      }
      val gotVarResult =
        lpModel.variables
          .allValues()
          .associate { lpVar ->
            Pair(lpVar.identifier, lpVar.result)
          }
      assertEquals(wantVarResult, gotVarResult, "results want $wantVarResult got $gotVarResult")
      val gotObjectiveValue = lpModel.solution!!.objective!!
      assertTrue(
        Math.abs(wantObjectiveVal - gotObjectiveValue) < 0.01,
        "objcetive value want $wantObjectiveVal got $gotObjectiveValue",
      )
    }
  }
}