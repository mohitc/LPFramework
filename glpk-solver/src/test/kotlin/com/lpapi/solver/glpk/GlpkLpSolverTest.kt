package com.lpapi.solver.glpk

import com.lpapi.model.LPConstraint
import com.lpapi.model.LPExpression
import com.lpapi.model.LPModel
import com.lpapi.model.LPVar
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.gnu.glpk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.mock
import java.lang.RuntimeException
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GlpkLpSolverTest {

  private val log = KotlinLogging.logger { this.javaClass.name }

  companion object {
    val mockedModel = mock<glp_prob> {}
    val glpIocp = glp_iocp()
    val mockSwigTypePInt = mock<SWIGTYPE_p_int> {}
    val mockSwigTypePDouble = mock<SWIGTYPE_p_double> {}
  }

  private fun setParameter(solver: GlpkLpSolver, field: String, value: Any?) {
    solver.javaClass.getDeclaredField(field).let {
      it.isAccessible = true
      it.set(solver, value)
    }
  }

  private fun setVariableMap(solver: GlpkLpSolver, variableMap: MutableMap<String, Int>) =
    setParameter(solver, "variableMap", variableMap)

  private fun setConstraintMap(solver: GlpkLpSolver, variableMap: MutableMap<String, Int>) =
    setParameter(solver, "constraintMap", variableMap)

  private fun argsForInitModel() = Stream.of(
    Arguments.of(
      "Exception in glp_create_prob() results in a failure",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test-model"),
      false,
      null,
    ),
    Arguments.of(
      "Exception in glp_set_prob_name() results in a failure",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Unit> { GLPK.glp_set_prob_name(mockedModel, "test-model") }
          .thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test-model"),
      false,
      null,
    ),
    Arguments.of(
      "Model is initialized correctly",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        return { mockedStatic.close() }
      },
      LPModel("test-model"),
      true,
      mockedModel,
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitModel")
  fun testInitModel(
    desc: String,
    initMock: () -> (() -> Unit),
    model: LPModel,
    wantSuccess: Boolean,
    wantModel: glp_prob?
  ) {
    log.info { "Test Case: $desc" }
    val cleanup = initMock()
    try {
      val solver = GlpkLpSolver(model)
      val gotSuccess = solver.initModel()
      assertEquals(wantSuccess, gotSuccess, "solver.initModel()")
      if (wantSuccess) {
        assertEquals(wantModel, solver.getBaseModel(), "solver.glpkModel")
      }
    } finally {
      cleanup()
    }
  }

  private fun argsForInitVars() = Stream.of(
    Arguments.of(
      "Exception in glp_add_cols() results in a failure",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_add_cols(mockedModel, 1) }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPVar("x", LPVarType.BOOLEAN),
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Exception in glp_set_col_name() results in a failure",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_cols(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<Unit> { GLPK.glp_set_col_name(mockedModel, 1, "x") }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPVar("x", LPVarType.BOOLEAN),
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Exception in glp_set_col_kind() results in a failure",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_cols(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<Unit> { GLPK.glp_set_col_kind(mockedModel, 1, GLPKConstants.GLP_BV) }
          .thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPVar("x", LPVarType.BOOLEAN),
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Exception in glp_set_col_bnds() results in a failure",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_cols(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<Unit> {
          GLPK.glp_set_col_bnds(
            mockedModel, 1,
            GLPKConstants.GLP_DB, 0.0, 1.0
          )
        }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPVar("x", LPVarType.BOOLEAN, 0, 1),
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Exception in glp_set_col_bnds() results in a failure (check for fixed value bound type)",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_cols(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<Unit> {
          GLPK.glp_set_col_bnds(
            mockedModel, 1, GLPKConstants.GLP_FX,
            1.0, 1.0
          )
        }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPVar("x", LPVarType.BOOLEAN, 1, 1),
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Success Case",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_cols(mockedModel, 1) }.thenReturn(1)
        return { mockedStatic.close() }
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
    initMock: () -> (() -> Unit),
    lpVar: LPVar,
    wantSuccess: Boolean,
    wantVarMap: Map<String, Int>
  ) {
    log.info { "Test Case $desc" }
    val cleanup = initMock()
    try {
      val model = LPModel("test")
      model.variables.add(lpVar)
      val solver = GlpkLpSolver(model)
      solver.initModel()
      val gotVarMap = mutableMapOf<String, Int>().apply {
        setVariableMap(solver, this)
      }
      val gotSuccess = solver.initVars()
      assertEquals(wantSuccess, gotSuccess, "solver.initVars()")
      assertEquals(gotVarMap, wantVarMap, "solver.variableMap")
    } finally {
      cleanup()
    }
  }

  private fun argsForGetSolutionStatus() = Stream.of(
    Arguments.of(
      "Undefined goes to LPSolutionStatus Unknown",
      GLPKConstants.GLP_UNDEF, LPSolutionStatus.UNKNOWN,
    ),
    Arguments.of(
      "Optimal goes to LPSolutionStatus Optimal",
      GLPKConstants.GLP_OPT, LPSolutionStatus.OPTIMAL,
    ),
    Arguments.of(
      "Feasible goes to LPSolutionStatus Time limited",
      GLPKConstants.GLP_FEAS, LPSolutionStatus.TIME_LIMIT,
    ),
    Arguments.of(
      "Infeasible goes to LPSolutionStatus Infeasible",
      GLPKConstants.GLP_INFEAS, LPSolutionStatus.INFEASIBLE,
    ),
    Arguments.of(
      "Unbounded goes to Unbounded",
      GLPKConstants.GLP_UNBND, LPSolutionStatus.UNBOUNDED,
    ),
    Arguments.of(
      "Any other value goes to unknown",
      -1, LPSolutionStatus.UNKNOWN,
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForGetSolutionStatus")
  fun testGetSolutionStatus(desc: String, glpkStatus: Int, wantStatus: LPSolutionStatus) {
    log.info { "Test Case: $desc" }
    val model = LPModel()
    val solver = GlpkLpSolver(model)
    assertEquals(wantStatus, solver.getSolutionStatus(glpkStatus), "solver.getSolutionStatus($glpkStatus)")
  }

  private fun argsForInitObjectiveFunction() = Stream.of(
    Arguments.of(
      "glp_set_obj_dir() results in an error",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Unit?> { GLPK.glp_set_obj_dir(mockedModel, GLPKConstants.GLP_MIN) }
          .thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test"),
      mutableMapOf<String, Int>(),
      LPObjectiveType.MINIMIZE,
      LPExpression(),
      false
    ),
    Arguments.of(
      "glp_set_obj_dir() results in an error (Maximize)",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Unit?> { GLPK.glp_set_obj_dir(mockedModel, GLPKConstants.GLP_MAX) }
          .thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test"),
      mutableMapOf<String, Int>(),
      LPObjectiveType.MAXIMIZE,
      LPExpression(),
      false
    ),
    Arguments.of(
      "Failure to reduce objective results in a false value",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.BOOLEAN))
        this.variables.add(LPVar("y", LPVarType.BOOLEAN))
      },
      mutableMapOf(Pair("x", 1), Pair("y", 2)),
      LPObjectiveType.MINIMIZE,
      LPExpression().apply { this.addTerm("a", "x").addTerm(3, "y") },
      false
    ),
    Arguments.of(
      "Failure to reduce objective results in a false value",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.BOOLEAN))
        this.variables.add(LPVar("y", LPVarType.BOOLEAN))
      },
      mutableMapOf(Pair("x", 1), Pair("y", 2)),
      LPObjectiveType.MINIMIZE,
      LPExpression().apply { this.addTerm("a", "x").addTerm(3, "y") },
      false
    ),
    Arguments.of(
      "glp_set_obj_coef throws runtime exception on setting constant",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Unit?> { GLPK.glp_set_obj_coef(mockedModel, 0, 3.0) }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.BOOLEAN))
      },
      mutableMapOf(Pair("x", 1)),
      LPObjectiveType.MAXIMIZE,
      LPExpression().apply { this.add(3).addTerm(2, "x") },
      false
    ),
    Arguments.of(
      "glp_set_obj_coef throws runtime exception on setting variable coefficient",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Unit?> { GLPK.glp_set_obj_coef(mockedModel, 1, 2.0) }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.BOOLEAN))
      },
      mutableMapOf(Pair("x", 1)),
      LPObjectiveType.MAXIMIZE,
      LPExpression().apply { this.add(3).addTerm(2, "x") },
      false
    ),
    Arguments.of(
      "Objective is configured successfully",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.BOOLEAN))
      },
      mutableMapOf(Pair("x", 1)),
      LPObjectiveType.MINIMIZE,
      LPExpression().apply { this.add(3).addTerm(2, "x") },
      true
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitObjectiveFunction")
  fun testInitObjectiveFunction(
    desc: String,
    initMock: () -> (() -> Unit),
    model: LPModel,
    varMap: MutableMap<String, Int>,
    objective: LPObjectiveType,
    expr: LPExpression,
    wantSuccess: Boolean
  ) {
    log.info { "Test Case: $desc" }
    val cleanup = initMock()
    try {
      val solver = GlpkLpSolver(model)
      model.objective.expression = expr
      model.objective.objective = objective
      setVariableMap(solver, varMap)
      solver.initModel()
      val gotSuccess = solver.initObjectiveFunction()
      assertEquals(wantSuccess, gotSuccess, "solver.initObjectiveFunction()")
    } finally {
      cleanup()
    }
  }

  private fun argsForSolve() = Stream.of(
    Arguments.of(
      "Exception on glp_intopt results in an errored status",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_intopt(mockedModel, glpIocp) }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.BOOLEAN))
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1)))
        this.initModel()
        setParameter(this, "intOptConfig", fun (): glp_iocp { return glpIocp })
      },
      LPSolutionStatus.ERROR,
      mutableMapOf<String, Number>()
    ),
    Arguments.of(
      "Undefined results in a solution status of unknown",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_mip_status(mockedModel) }.thenReturn(GLPKConstants.GLP_UNDEF)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.BOOLEAN))
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1)))
        this.initModel()
        setParameter(this, "intOptConfig", fun (): glp_iocp { return glpIocp })
      },
      LPSolutionStatus.UNKNOWN,
      mutableMapOf<String, Number>()
    ),
    Arguments.of(
      "Error in result extraction results in an errored state",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_mip_status(mockedModel) }.thenReturn(GLPKConstants.GLP_OPT)
        mockedStatic.`when`<Double?> { GLPK.glp_mip_col_val(mockedModel, 1) }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1)))
        this.initModel()
        setParameter(this, "intOptConfig", fun (): glp_iocp { return glpIocp })
      },
      LPSolutionStatus.ERROR,
      mutableMapOf<String, Number>()
    ),
    Arguments.of(
      "Known solution results in the population of the results",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_mip_status(mockedModel) }.thenReturn(GLPKConstants.GLP_OPT)
        mockedStatic.`when`<Double?> { GLPK.glp_mip_col_val(mockedModel, 1) }.thenReturn(3.2)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1)))
        this.initModel()
        setParameter(this, "intOptConfig", fun (): glp_iocp { return glpIocp })
      },
      LPSolutionStatus.OPTIMAL,
      mutableMapOf<String, Number>(Pair("x", 3))
    ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForSolve")
  fun testSolve(
    desc: String,
    initMock: () -> (() -> Unit),
    model: LPModel,
    initSolver: (LPModel) -> GlpkLpSolver,
    wantStatus: LPSolutionStatus,
    wantResults: MutableMap<String, Number>
  ) {
    log.info { "Test Case: $desc" }
    val cleanup = initMock()
    try {
      val solver = initSolver(model)
      val gotStatus = solver.solve()
      assertEquals(wantStatus, gotStatus, "solver.solve()")
      val gotResults = model.variables.allValues()
          .filter { it.resultSet }.associate { Pair(it.identifier, it.result) }
      assertEquals(wantResults, gotResults, "model.results")
    } finally {
      cleanup()
    }
  }

  private fun argsForInitConstraints() = Stream.of(
    Arguments.of(
      "Failure to reduce a constraint results in false",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("a", "x").addTerm("b", "y")
            this.operator = LPOperator.LESS_EQUAL
            this.rhs.add(10)
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
        this.initModel()
      },
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Assert that constraint row name is set correctly",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_rows(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<Unit?> { GLPK.glp_set_row_name(mockedModel, 1, "test-constraint") }
          .thenThrow(RuntimeException())
        mockedStatic.`when`<SWIGTYPE_p_int?> { GLPK.new_intArray(2) }.thenReturn(mockSwigTypePInt)
        mockedStatic.`when`<SWIGTYPE_p_double?> { GLPK.new_doubleArray(2) }.thenReturn(mockSwigTypePDouble)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("x").addTerm("y")
            this.operator = LPOperator.LESS_EQUAL
            this.rhs.add(10)
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
        this.initModel()
      },
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "LessEqual results in a call to set row bounds as GLPKConstants.GLP_UP",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_rows(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<Int?> {
          GLPK.glp_set_row_bnds(
            mockedModel, 1, GLPKConstants.GLP_UP,
            0.0, 10.0
          )
        }.thenThrow(RuntimeException())
        mockedStatic.`when`<SWIGTYPE_p_int?> { GLPK.new_intArray(2) }.thenReturn(mockSwigTypePInt)
        mockedStatic.`when`<SWIGTYPE_p_double?> { GLPK.new_doubleArray(2) }.thenReturn(mockSwigTypePDouble)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("x").addTerm(-2, "y")
            this.operator = LPOperator.LESS_EQUAL
            this.rhs.add(10)
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
        this.initModel()
      },
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "GreaterEqual results in a call to set row bounds as GLPKConstants.GLP_LO",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_rows(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<Int?> {
          GLPK.glp_set_row_bnds(
            mockedModel, 1, GLPKConstants.GLP_LO,
            10.0, 0.0
          )
        }.thenThrow(RuntimeException())
        mockedStatic.`when`<SWIGTYPE_p_int?> { GLPK.new_intArray(2) }.thenReturn(mockSwigTypePInt)
        mockedStatic.`when`<SWIGTYPE_p_double?> { GLPK.new_doubleArray(2) }.thenReturn(mockSwigTypePDouble)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("x").addTerm(-2, "y")
            this.operator = LPOperator.GREATER_EQUAL
            this.rhs.add(10)
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
        this.initModel()
      },
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Equals results in a call to set row bounds as GLPKConstants.GLP_FX",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_rows(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<Int?> {
          GLPK.glp_set_row_bnds(
            mockedModel, 1, GLPKConstants.GLP_FX,
            10.0, 10.0
          )
        }.thenThrow(RuntimeException())
        mockedStatic.`when`<SWIGTYPE_p_int?> { GLPK.new_intArray(2) }.thenReturn(mockSwigTypePInt)
        mockedStatic.`when`<SWIGTYPE_p_double?> { GLPK.new_doubleArray(2) }.thenReturn(mockSwigTypePDouble)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("x").addTerm(-2, "y")
            this.operator = LPOperator.EQUAL
            this.rhs.add(10)
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
        this.initModel()
      },
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Assert that variable indexes in the models are set correctly",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_rows(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<SWIGTYPE_p_int?> { GLPK.new_intArray(2) }.thenReturn(mockSwigTypePInt)
        mockedStatic.`when`<SWIGTYPE_p_double?> { GLPK.new_doubleArray(2) }.thenReturn(mockSwigTypePDouble)
        mockedStatic.`when`<Unit> { GLPK.intArray_setitem(mockSwigTypePInt, 2, 2) }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("x").addTerm(-2, "y").add(10)
            this.operator = LPOperator.LESS_EQUAL
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
        this.initModel()
      },
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Assert that variable coefficients in the models are set correctly",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_rows(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<SWIGTYPE_p_int?> { GLPK.new_intArray(2) }.thenReturn(mockSwigTypePInt)
        mockedStatic.`when`<SWIGTYPE_p_double?> { GLPK.new_doubleArray(2) }.thenReturn(mockSwigTypePDouble)
        mockedStatic.`when`<Unit> { GLPK.doubleArray_setitem(mockSwigTypePDouble, 2, -2.0) }
          .thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("x").addTerm(-2, "y").add(10)
            this.operator = LPOperator.LESS_EQUAL
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
        this.initModel()
      },
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Assert that row is configured correctly",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_rows(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<SWIGTYPE_p_int?> { GLPK.new_intArray(2) }.thenReturn(mockSwigTypePInt)
        mockedStatic.`when`<SWIGTYPE_p_double?> { GLPK.new_doubleArray(2) }.thenReturn(mockSwigTypePDouble)
        mockedStatic.`when`<Unit?> {
          GLPK.glp_set_mat_row(
            mockedModel, 1, 2,
            mockSwigTypePInt, mockSwigTypePDouble
          )
        }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("x").addTerm(-2, "y").add(10)
            this.operator = LPOperator.LESS_EQUAL
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
        this.initModel()
      },
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Error in cleanup (delete int_array) is handled correctly",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_rows(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<SWIGTYPE_p_int?> { GLPK.new_intArray(2) }.thenReturn(mockSwigTypePInt)
        mockedStatic.`when`<SWIGTYPE_p_double?> { GLPK.new_doubleArray(2) }.thenReturn(mockSwigTypePDouble)
        mockedStatic.`when`<Unit?> { GLPK.delete_intArray(mockSwigTypePInt) }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("x").addTerm(-2, "y").add(10)
            this.operator = LPOperator.GREATER_EQUAL
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
        this.initModel()
      },
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Error in cleanup (delete_doubleArray) is handled correctly",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_rows(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<SWIGTYPE_p_int?> { GLPK.new_intArray(2) }.thenReturn(mockSwigTypePInt)
        mockedStatic.`when`<SWIGTYPE_p_double?> { GLPK.new_doubleArray(2) }.thenReturn(mockSwigTypePDouble)
        mockedStatic.`when`<Unit?> { GLPK.delete_doubleArray(mockSwigTypePDouble) }.thenThrow(RuntimeException())
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("x").addTerm(-2, "y").add(10)
            this.operator = LPOperator.EQUAL
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
        setVariableMap(this, mutableMapOf(Pair("x", 1), Pair("y", 2)))
        this.initModel()
      },
      false,
      mutableMapOf<String, Int>(),
    ),
    Arguments.of(
      "Success case",
      fun(): (() -> Unit) {
        val mockedStatic = Mockito.mockStatic(GLPK::class.java)
        mockedStatic.`when`<glp_prob?> { GLPK.glp_create_prob() }.thenReturn(mockedModel)
        mockedStatic.`when`<Int?> { GLPK.glp_add_rows(mockedModel, 1) }.thenReturn(1)
        mockedStatic.`when`<SWIGTYPE_p_int?> { GLPK.new_intArray(2) }.thenReturn(mockSwigTypePInt)
        mockedStatic.`when`<SWIGTYPE_p_double?> { GLPK.new_doubleArray(2) }.thenReturn(mockSwigTypePDouble)
        return { mockedStatic.close() }
      },
      LPModel("test").apply {
        this.variables.add(LPVar("x", LPVarType.INTEGER, 0, 10))
        this.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
        this.constraints.add(
          LPConstraint("test-constraint").apply {
            this.lhs.addTerm("x").addTerm(-2, "y").add(10)
            this.operator = LPOperator.EQUAL
          }
        )
      },
      fun(model: LPModel): GlpkLpSolver = GlpkLpSolver(model).apply {
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
    initMock: () -> (() -> Unit),
    model: LPModel,
    initSolver: (LPModel) -> GlpkLpSolver,
    wantSuccess: Boolean,
    wantConstraintMap: MutableMap<String, Int>
  ) {
    log.info { "Test Case : $desc" }
    val cleanup = initMock()
    try {
      val solver = initSolver(model)
      val gotConstraintMap = mutableMapOf<String, Int>().apply {
        setConstraintMap(solver, this)
      }
      val gotSuccess = solver.initConstraints()
      assertEquals(wantSuccess, gotSuccess, "solver.initConstraints()")
      assertEquals(gotConstraintMap, wantConstraintMap, "solver.variableMap")
    } finally {
      cleanup()
    }
  }
}