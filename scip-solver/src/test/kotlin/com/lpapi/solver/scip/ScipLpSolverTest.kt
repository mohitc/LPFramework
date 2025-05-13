package com.lpapi.solver.scip

import com.lpapi.ffm.scip.Constraint
import com.lpapi.ffm.scip.SCIPProblem
import com.lpapi.ffm.scip.SCIPRetCode
import com.lpapi.ffm.scip.SCIPStatus
import com.lpapi.ffm.scip.SCIPVarType
import com.lpapi.ffm.scip.Variable
import io.github.mohitc.lpapi.model.LPConstant
import io.github.mohitc.lpapi.model.LPConstraint
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPVar
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import java.util.stream.Stream
import kotlin.RuntimeException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ScipLpSolverTest {
  private val log = KotlinLogging.logger { this.javaClass.name }

  companion object {
    val mockedModel: SCIPProblem =
      mock<SCIPProblem> {}.let { p ->
        `when`(p.createProblem("test-model")).thenReturn(SCIPRetCode.SCIP_OKAY)
        `when`(p.includeDefaultPlugins()).thenReturn(SCIPRetCode.SCIP_OKAY)
        p
      }
    val mockedVarX = mock<Variable> {}
    val mockedVarY = mock<Variable> {}
    val mockedVarZ = mock<Variable> {}
    val mockedConstraint1 = mock<Constraint> {}
    val mockedConstraint2 = mock<Constraint> {}
    val createdObjectiveCoefficients = mutableMapOf<Variable, Number>()
    const val SCIP_INF = 1E20
  }

  @Test
  fun testScipVarTypeComputation() {
    val model = ScipLpSolver(LPModel())
    LPVarType.values().forEach { lpVarType ->
      assertNotNull(
        model.getScipVarType(lpVarType),
        "Scip Model variable type not found for LP " +
          "type $lpVarType",
      )
    }
  }

  private fun setParameter(
    solver: ScipLpSolver,
    field: String,
    value: Any?,
  ) {
    solver.javaClass.getDeclaredField(field).let {
      it.isAccessible = true
      it.set(solver, value)
    }
  }

  private fun setVariableMap(
    solver: ScipLpSolver,
    varMap: MutableMap<String, Variable>,
  ) {
    setParameter(solver, "variableMap", varMap)
  }

  private fun setConstraintMap(
    solver: ScipLpSolver,
    constraintMap: MutableMap<String, Constraint>,
  ) {
    setParameter(solver, "constraintMap", constraintMap)
  }

  private fun setScipModel(
    solver: ScipLpSolver,
    model: SCIPProblem,
  ) {
    setParameter(solver, "scipModel", model)
  }

  private fun argsForInitModel() =
    Stream.of(
      Arguments.of(
        "Exception in createProblem() results in a failure",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createProblem("test-model")).thenThrow(RuntimeException())
          `when`(mockedModel.includeDefaultPlugins()).thenReturn(SCIPRetCode.SCIP_OKAY)
          return mockedModel
        },
        LPModel("test-model"),
        false,
        null,
      ),
      Arguments.of(
        "Invalid error code in Scip.create() results in a failure",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createProblem("test-model")).thenReturn(SCIPRetCode.SCIP_UNKNOWN)
          `when`(mockedModel.includeDefaultPlugins()).thenReturn(SCIPRetCode.SCIP_OKAY)
          return mockedModel
        },
        LPModel("test-model"),
        false,
        null,
      ),
      Arguments.of(
        "Exception in includeDefaultPlugins() results in a failure",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createProblem("test-model")).thenReturn(SCIPRetCode.SCIP_OKAY)
          `when`(mockedModel.includeDefaultPlugins()).thenThrow(RuntimeException())
          return mockedModel
        },
        LPModel("test-model"),
        false,
        null,
      ),
      Arguments.of(
        "Invalid error code in includeDefaultPlugins() results in a failure",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createProblem("test-model")).thenReturn(SCIPRetCode.SCIP_OKAY)
          `when`(mockedModel.includeDefaultPlugins()).thenReturn(SCIPRetCode.SCIP_BRANCHERROR)
          return mockedModel
        },
        LPModel("test-model"),
        false,
        null,
      ),
      Arguments.of(
        "Model is initialized correctly",
        fun(): SCIPProblem = mockedModel,
        LPModel("test-model"),
        true,
        mockedModel,
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitModel")
  fun testInitModel(
    desc: String,
    initMock: () -> SCIPProblem,
    model: LPModel,
    wantSuccess: Boolean,
    wantModel: SCIPProblem?,
  ) {
    log.info { "Test Case: $desc" }
    val mockedModel = initMock()
    val solver = ScipLpSolver(model)
    setScipModel(solver, mockedModel)
    val gotSuccess = solver.initModel()
    assertEquals(wantSuccess, gotSuccess, "solver.initModel()")
    if (wantSuccess) {
      assertEquals(wantModel, solver.getBaseModel(), "solver.scipModel")
    }
  }

  private fun argsForInitVars() =
    Stream.of(
      Arguments.of(
        "Exception while creating a boolean variable",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(
            mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY),
          ).thenThrow(RuntimeException())
          return mockedModel
        },
        listOf(LPVar("X", LPVarType.BOOLEAN)),
        false,
        mutableMapOf<String, Variable>(),
      ),
      Arguments.of(
        "Successful creation of a boolean variable",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          return mockedModel
        },
        listOf(LPVar("X", LPVarType.BOOLEAN)),
        true,
        mutableMapOf(Pair("X", mockedVarX)),
      ),
      Arguments.of(
        "Successful creation of a linear variable",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(
            mockedModel.createVar("Y", -1.0, 10.0, 0.0, SCIPVarType.SCIP_VARTYPE_CONTINUOUS),
          ).thenReturn(mockedVarY)
          return mockedModel
        },
        listOf(LPVar("Y", LPVarType.DOUBLE, -1.0, 10.0)),
        true,
        mutableMapOf(Pair("Y", mockedVarY)),
      ),
      Arguments.of(
        "Successful creation of an integer variable",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("Z", -1.3, 20.2, 0.0, SCIPVarType.SCIP_VARTYPE_INTEGER)).thenReturn(mockedVarZ)
          return mockedModel
        },
        listOf(LPVar("Z", LPVarType.INTEGER, -1.3, 20.2)),
        true,
        mutableMapOf(Pair("Z", mockedVarZ)),
      ),
      Arguments.of(
        "Successful creation of multiple variables",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(
            mockedModel.createVar("Y", -1.0, 10.0, 0.0, SCIPVarType.SCIP_VARTYPE_CONTINUOUS),
          ).thenReturn(mockedVarY)
          `when`(mockedModel.createVar("Z", -1.3, 20.2, 0.0, SCIPVarType.SCIP_VARTYPE_INTEGER)).thenReturn(mockedVarZ)
          return mockedModel
        },
        listOf(
          LPVar("X", LPVarType.BOOLEAN),
          LPVar("Y", LPVarType.DOUBLE, -1.0, 10.0),
          LPVar("Z", LPVarType.INTEGER, -1.3, 20.2),
        ),
        true,
        mutableMapOf(
          Pair("X", mockedVarX),
          Pair("Y", mockedVarY),
          Pair("Z", mockedVarZ),
        ),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitVars")
  fun testInitVars(
    desc: String,
    initMock: () -> SCIPProblem,
    lpVars: List<LPVar>,
    wantSuccess: Boolean,
    wantVarMap: Map<String, Variable>,
  ) {
    log.info { "Test Case $desc" }
    val mockedScipModel = initMock()
    val model = LPModel("test")
    lpVars.forEach { lpVar -> model.variables.add(lpVar) }
    val solver = ScipLpSolver(model)
    setScipModel(solver, mockedScipModel)
    solver.initModel()
    val gotVarMap =
      mutableMapOf<String, Variable>().apply {
        setVariableMap(solver, this)
      }
    val gotSuccess = solver.initVars()
    assertEquals(wantSuccess, gotSuccess, "solver.initVars()")
    assertEquals(gotVarMap, wantVarMap, "solver.variableMap")
  }

  private fun argsForInitConstraints() =
    Stream.of(
      Arguments.of(
        "Exception while initializing a constraint",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(
            mockedModel.createConstraint("constraint1", listOf(mockedVarX), listOf(1.0), 1.0, 1.0),
          ).thenThrow(RuntimeException())
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          val constr = LPConstraint("constraint1")
          constr.lhs.addTerm("X").add(-1)
          constr.operator = LPOperator.EQUAL
          model.constraints.add(constr)
          return model
        },
        false,
        mutableMapOf(
          Pair("X", mockedVarX),
        ),
        mutableMapOf<String, Constraint>(),
      ),
      Arguments.of(
        "Null value while adding Constraint",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(
            mockedModel.createConstraint("constraint1", listOf(mockedVarX), listOf(1.0), 1.0, 1.0),
          ).thenReturn(null)
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          val constr = LPConstraint("constraint1")
          constr.lhs.addTerm("X").add(-1)
          constr.operator = LPOperator.EQUAL
          model.constraints.add(constr)
          return model
        },
        false,
        mutableMapOf(
          Pair("X", mockedVarX),
        ),
        mutableMapOf<String, Constraint>(),
      ),
      Arguments.of(
        "Successful initialization of a single equality constraint",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_INTEGER)).thenReturn(mockedVarY)
          `when`(
            mockedModel.createConstraint("constraint1", listOf(mockedVarX, mockedVarY), listOf(1.0, -2.0), 1.0, 1.0),
          ).thenReturn(mockedConstraint1)
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          model.variables.add(LPVar("Y", LPVarType.INTEGER, 0.0, 1.0))
          val constr = LPConstraint("constraint1")
          constr.lhs.addTerm("X").add(-1)
          constr.operator = LPOperator.EQUAL
          constr.rhs.addTerm(2.0, "Y")
          model.constraints.add(constr)
          return model
        },
        true,
        mutableMapOf(
          Pair("X", mockedVarX),
          Pair("Y", mockedVarY),
        ),
        mutableMapOf(Pair("constraint1", mockedConstraint1)),
      ),
      Arguments.of(
        "Successful initialization of a <= constraint (2x + 3y <= 5)",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
          `when`(
            mockedModel.createVar("Z", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_CONTINUOUS),
          ).thenReturn(mockedVarZ)
          `when`(mockedModel.infinity()).thenReturn(SCIP_INF)
          `when`(
            mockedModel.createConstraint(
              "constraint1",
              listOf(mockedVarX, mockedVarY),
              listOf(2.0, 3.0),
              -1.0 * SCIP_INF,
              5.0,
            ),
          ).thenReturn(mockedConstraint1)
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          model.variables.add(LPVar("Y", LPVarType.BOOLEAN, 0.0, 1.0))
          model.variables.add(LPVar("Z", LPVarType.DOUBLE, 0, 1))
          val constr = LPConstraint("constraint1")
          constr.lhs.addTerm(2, "X").add(-5)
          constr.operator = LPOperator.LESS_EQUAL
          constr.rhs.addTerm(-3.0, "Y")
          model.constraints.add(constr)
          return model
        },
        true,
        mutableMapOf(
          Pair("X", mockedVarX),
          Pair("Y", mockedVarY),
          Pair("Z", mockedVarZ),
        ),
        mutableMapOf(Pair("constraint1", mockedConstraint1)),
      ),
      Arguments.of(
        "Successful initialization of a >= constraint (3y + 4x >= 3)",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
          `when`(
            mockedModel.createVar("Z", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_CONTINUOUS),
          ).thenReturn(mockedVarZ)
          `when`(mockedModel.infinity()).thenReturn(SCIP_INF)
          `when`(
            mockedModel.createConstraint(
              "constraint1",
              listOf(mockedVarY, mockedVarX),
              listOf(3.0, 4.0),
              3.0,
              SCIP_INF,
            ),
          ).thenReturn(mockedConstraint1)
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          model.variables.add(LPVar("Y", LPVarType.BOOLEAN, 0.0, 1.0))
          model.variables.add(LPVar("Z", LPVarType.DOUBLE, 0, 1))
          val constr = LPConstraint("constraint1")
          constr.lhs
            .addTerm(2, "Y")
            .addTerm(2, "X")
            .add(-1)
          constr.operator = LPOperator.GREATER_EQUAL
          constr.rhs
            .addTerm(-1.0, "Y")
            .addTerm(-2.0, "X")
            .add(2)
          model.constraints.add(constr)
          return model
        },
        true,
        mutableMapOf(
          Pair("X", mockedVarX),
          Pair("Y", mockedVarY),
          Pair("Z", mockedVarZ),
        ),
        mutableMapOf(Pair("constraint1", mockedConstraint1)),
      ),
      Arguments.of(
        "Successful initialization of multiple constraints (1) aX + by + cZ <= 4 (2) X + Y >= 2",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
          `when`(mockedModel.createVar("Z", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarZ)
          `when`(mockedModel.infinity()).thenReturn(SCIP_INF)
          `when`(
            mockedModel.createConstraint(
              "constraint1",
              listOf(mockedVarX, mockedVarY, mockedVarZ),
              listOf(2.0, 3.0, 4.0),
              -1.0 * SCIP_INF,
              4.0,
            ),
          ).thenReturn(mockedConstraint1)
          `when`(
            mockedModel.createConstraint(
              "constraint2",
              listOf(mockedVarX, mockedVarY),
              listOf(1.0, 1.0),
              2.0,
              SCIP_INF,
            ),
          ).thenReturn(mockedConstraint2)
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          model.variables.add(LPVar("Y", LPVarType.BOOLEAN))
          model.variables.add(LPVar("Z", LPVarType.BOOLEAN))
          model.constants.add(LPConstant("a", 2))
          model.constants.add(LPConstant("b", 3))
          model.constants.add(LPConstant("c", 4))
          val constr1 = LPConstraint("constraint1")
          constr1.lhs
            .addTerm("a", "X")
            .addTerm("b", "Y")
            .addTerm("c", "Z")
            .add(-4)
          constr1.operator = LPOperator.LESS_EQUAL
          model.constraints.add(constr1)
          val constr2 = LPConstraint("constraint2")
          constr2.lhs.addTerm("X").addTerm("Y")
          constr2.operator = LPOperator.GREATER_EQUAL
          constr2.rhs.add(2)
          model.constraints.add(constr2)
          return model
        },
        true,
        mutableMapOf(
          Pair("X", mockedVarX),
          Pair("Y", mockedVarY),
          Pair("Z", mockedVarZ),
        ),
        mutableMapOf(
          Pair("constraint1", mockedConstraint1),
          Pair("constraint2", mockedConstraint2),
        ),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitConstraints")
  fun testInitConstraints(
    desc: String,
    initMock: () -> SCIPProblem,
    initModel: () -> LPModel,
    wantSuccess: Boolean,
    wantVarMap: Map<String, Variable>,
    wantConstraintMap: Map<String, Constraint>,
  ) {
    log.info { "Test Case $desc" }
    val mockedScipModel = initMock()
    val model = initModel()
    val solver = ScipLpSolver(model)
    setScipModel(solver, mockedScipModel)
    solver.initModel()
    val gotVarMap =
      mutableMapOf<String, Variable>().apply {
        setVariableMap(solver, this)
      }
    val gotConstraintMap =
      mutableMapOf<String, Constraint>().apply {
        setConstraintMap(solver, this)
      }
    val varSuccess = solver.initVars()
    assertEquals(varSuccess, true, "solver.initVars")
    val gotSuccess = solver.initConstraints()
    assertEquals(wantSuccess, gotSuccess, "solver.initConstraints()")
    assertEquals(gotVarMap, wantVarMap, "solver.variableMap")
    assertEquals(gotConstraintMap, wantConstraintMap, "solver.constraintMap")
  }

  private fun argsForInitObjective() =
    Stream.of(
      Arguments.of(
        "Exception while initializing the objective function",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(mockedModel.setVariableObjective(mockedVarX, 1.0)).thenThrow(RuntimeException())
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          model.objective.expression.addTerm("X")
          return model
        },
        false,
        mutableMapOf(
          Pair("X", mockedVarX),
        ),
        mutableMapOf<Variable, Number>(),
      ),
      Arguments.of(
        "Exception while initializing the objective direction",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(
            mockedModel.setVariableObjective(mockedVarX, 1.0),
          ).then { createdObjectiveCoefficients.put(mockedVarX, 1.0) }
          `when`(mockedModel.maximize()).thenThrow(RuntimeException())
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          model.objective.expression.addTerm("X")
          model.objective.objective = LPObjectiveType.MAXIMIZE
          return model
        },
        false,
        mutableMapOf(
          Pair("X", mockedVarX),
        ),
        mutableMapOf<Variable, Number>(Pair(mockedVarX, 1.0)),
      ),
      Arguments.of(
        "Reduced expression with maximization objective  5x + 2y -2x + 2y",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
          `when`(
            mockedModel.setVariableObjective(mockedVarX, 3.0),
          ).then { createdObjectiveCoefficients.put(mockedVarX, 3.0) }
          `when`(
            mockedModel.setVariableObjective(mockedVarY, 4.0),
          ).then { createdObjectiveCoefficients.put(mockedVarY, 4.0) }
          // Throw exception on invalid call
          `when`(mockedModel.minimize()).thenThrow(RuntimeException())
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          model.variables.add(LPVar("Y", LPVarType.BOOLEAN))
          model.objective.expression
            .addTerm(5, "X")
            .addTerm(2, "Y")
            .addTerm(-2, "X")
            .addTerm(2, "Y")
          model.objective.objective = LPObjectiveType.MAXIMIZE
          return model
        },
        true,
        mutableMapOf(
          Pair("X", mockedVarX),
          Pair("Y", mockedVarY),
        ),
        mutableMapOf<Variable, Number>(
          Pair(mockedVarX, 3.0),
          Pair(mockedVarY, 4.0),
        ),
      ),
      Arguments.of(
        "Reduced expression with minimization objective  5x + 2y -2x - 2y",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
          `when`(
            mockedModel.setVariableObjective(mockedVarX, 3.0),
          ).then { createdObjectiveCoefficients.put(mockedVarX, 3.0) }
          // Throw exception on invalid call
          `when`(mockedModel.maximize()).thenThrow(RuntimeException())
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          model.variables.add(LPVar("Y", LPVarType.BOOLEAN))
          model.objective.expression
            .addTerm(5, "X")
            .addTerm(2, "Y")
            .addTerm(-2, "X")
            .addTerm(-2, "Y")
          model.objective.objective = LPObjectiveType.MINIMIZE
          return model
        },
        true,
        mutableMapOf(
          Pair("X", mockedVarX),
          Pair("Y", mockedVarY),
        ),
        mutableMapOf<Variable, Number>(
          Pair(mockedVarX, 3.0),
        ),
      ),
      Arguments.of(
        "Empty Objective function (feasibility problem)",
        fun(): SCIPProblem {
          val mockedModel = mock<SCIPProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIPVarType.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
          return mockedModel
        },
        fun(): LPModel {
          val model = LPModel("test")
          model.variables.add(LPVar("X", LPVarType.BOOLEAN))
          model.variables.add(LPVar("Y", LPVarType.BOOLEAN))
          return model
        },
        true,
        mutableMapOf(
          Pair("X", mockedVarX),
          Pair("Y", mockedVarY),
        ),
        mutableMapOf<Variable, Number>(),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitObjective")
  fun testInitObjective(
    desc: String,
    initMock: () -> SCIPProblem,
    initModel: () -> LPModel,
    wantSuccess: Boolean,
    wantVarMap: Map<String, Variable>,
    wantObjectiveCoefficients: Map<Variable, Number>,
  ) {
    log.info { "Test Case $desc" }
    createdObjectiveCoefficients.clear()
    val mockedScipModel = initMock()
    val model = initModel()
    val solver = ScipLpSolver(model)
    setScipModel(solver, mockedScipModel)
    solver.initModel()
    val gotVarMap =
      mutableMapOf<String, Variable>().apply {
        setVariableMap(solver, this)
      }
    val varSuccess = solver.initVars()
    assertEquals(varSuccess, true, "solver.initVars")
    val gotSuccess = solver.initObjectiveFunction()
    assertEquals(wantSuccess, gotSuccess, "solver.initObjectiveFunction()")
    assertEquals(gotVarMap, wantVarMap, "solver.variableMap")
    assertEquals(createdObjectiveCoefficients, wantObjectiveCoefficients, "solver.changeObjVal calls")
  }

  private fun argsForGetSolutionStatus() =
    Stream.of(
      Arguments.of(
        "Optimal -> Optimal",
        SCIPStatus.OPTIMAL,
        LPSolutionStatus.OPTIMAL,
      ),
      Arguments.of(
        "Infeasible -> Infeasible",
        SCIPStatus.INFEASIBLE,
        LPSolutionStatus.INFEASIBLE,
      ),
      Arguments.of(
        "Unbounded -> Unbounded",
        SCIPStatus.UNBOUNDED,
        LPSolutionStatus.UNBOUNDED,
      ),
      Arguments.of(
        "Time limit -> Time limit",
        SCIPStatus.TIME_LIMIT,
        LPSolutionStatus.TIME_LIMIT,
      ),
      Arguments.of(
        "Infeasible or Unbounded -> Infeasible or Unbounded",
        SCIPStatus.INFEASIBLE_OR_UNBOUNDED,
        LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED,
      ),
      Arguments.of(
        "Unknown -> Unknown",
        SCIPStatus.UNKNOWN,
        LPSolutionStatus.UNKNOWN,
      ),
      Arguments.of(
        "Terminated -> Error",
        SCIPStatus.TERMINATED,
        LPSolutionStatus.ERROR,
      ),
      Arguments.of(
        "User Interrupted -> Error",
        SCIPStatus.USER_INTERRUPT,
        LPSolutionStatus.ERROR,
      ),
      Arguments.of(
        "Solution Limit -> CUTOFF",
        SCIPStatus.SOLUTION_LIMIT,
        LPSolutionStatus.CUTOFF,
      ),
      Arguments.of(
        "Gap Limit -> CUTOFF",
        SCIPStatus.GAP_LIMIT,
        LPSolutionStatus.CUTOFF,
      ),
      Arguments.of(
        "Best Solution Limit -> CUTOFF",
        SCIPStatus.BEST_SOLUTION_LIMIT,
        LPSolutionStatus.CUTOFF,
      ),
      Arguments.of(
        "Node Limit -> CUTOFF",
        SCIPStatus.NODE_LIMIT,
        LPSolutionStatus.CUTOFF,
      ),
      Arguments.of(
        "Total Node Limit -> CUTOFF",
        SCIPStatus.TOTAL_NODE_LIMIT,
        LPSolutionStatus.CUTOFF,
      ),
      Arguments.of(
        "Stall node Limit -> CUTOFF",
        SCIPStatus.STALL_NODE_LIMIT,
        LPSolutionStatus.CUTOFF,
      ),
      Arguments.of(
        "Dual Limit -> CUTOFF",
        SCIPStatus.DUAL_LIMIT,
        LPSolutionStatus.CUTOFF,
      ),
      Arguments.of(
        "Memory Limit -> CUTOFF",
        SCIPStatus.MEMORY_LIMIT,
        LPSolutionStatus.CUTOFF,
      ),
      Arguments.of(
        "Primal Limit -> CUTOFF",
        SCIPStatus.PRIMAL_LIMIT,
        LPSolutionStatus.CUTOFF,
      ),
      Arguments.of(
        "Restart Limit -> CUTOFF",
        SCIPStatus.RESTART_LIMIT,
        LPSolutionStatus.CUTOFF,
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForGetSolutionStatus")
  fun testGetSolutionStatus(
    desc: String,
    scipStatus: SCIPStatus,
    wantStatus: LPSolutionStatus,
  ) {
    log.info { "Test Case: $desc" }
    assertEquals(
      wantStatus,
      ScipLpSolver.getSolutionStatus(scipStatus),
      "solver.getSolutionStatus($scipStatus) want $wantStatus got ${ScipLpSolver.getSolutionStatus(scipStatus)}",
    )
  }
}