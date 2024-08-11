package com.lpapi.solver.scip

import com.lpapi.model.LPConstant
import com.lpapi.model.LPConstraint
import com.lpapi.model.LPModel
import com.lpapi.model.LPVar
import com.lpapi.model.enums.LPObjectiveType
import com.lpapi.model.enums.LPOperator
import com.lpapi.model.enums.LPVarType
import jscip.Constraint
import jscip.SCIP_Vartype
import jscip.Scip
import jscip.Solution
import jscip.Variable
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import java.util.stream.Stream
import kotlin.RuntimeException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ScipLpSolverTest {

  private val log = KotlinLogging.logger { this.javaClass.name }

  companion object {
    val mockedModel = mock<Scip> {}
    val mockedVarX = mock<Variable> {}
    val mockedVarY = mock<Variable> {}
    val mockedVarZ = mock<Variable> {}
    val mockedConstraint1 = mock<Constraint> {}
    val mockedConstraint2 = mock<Constraint> {}
    val createdConstraints = mutableSetOf<Constraint>()
    val createdObjectiveCoefficients = mutableMapOf<Variable, Number>()
    val mockedSolution = mock<Solution> {}
    const val SCIP_INF = 1E20
  }

  @Test
  fun testScipVarTypeComputation() {
    val model = ScipLpSolver(LPModel())
    LPVarType.values().forEach { lpVarType ->
      assertNotNull(
          model.getScipVarType(lpVarType),
          "Scip Model variable type not found for LP " +
              "type $lpVarType"
      )
    }
  }

  private fun setParameter(solver: ScipLpSolver, field: String, value: Any?) {
    solver.javaClass.getDeclaredField(field).let {
      it.isAccessible = true
      it.set(solver, value)
    }
  }

  private fun setVariableMap(solver: ScipLpSolver, varMap: MutableMap<String, Variable>) {
    setParameter(solver, "variableMap", varMap)
  }

  private fun setConstraintMap(solver: ScipLpSolver, constraintMap: MutableMap<String, Constraint>) {
    setParameter(solver, "constraintMap", constraintMap)
  }

  private fun setScipModel(solver: ScipLpSolver, model: Scip) {
    setParameter(solver, "scipModel", model)
  }


  private fun argsForInitModel() = Stream.of(
      Arguments.of(
          "Exception in Scip.create() results in a failure",
          fun(): Scip {
            val mockedModel = mock<Scip> {}
            `when`(mockedModel.create("test-model")).thenThrow(RuntimeException())
            return mockedModel
          },
          LPModel("test-model"),
          false,
          null,
      ),
      Arguments.of(
          "Model is initialized correctly",
          fun(): Scip {
            return mockedModel
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
      initMock: () -> Scip,
      model: LPModel,
      wantSuccess: Boolean,
      wantModel: Scip?
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

  private fun argsForInitVars() = Stream.of(
      Arguments.of(
          "Exception while creating a boolean variable",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenThrow(RuntimeException())
            return mockedModel
          },
          listOf( LPVar("X", LPVarType.BOOLEAN)),
          false,
          mutableMapOf<String, Variable>(),
      ),
      Arguments.of(
          "Successful creation of a boolean variable",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            return mockedModel
          },
          listOf( LPVar("X", LPVarType.BOOLEAN)),
          true,
          mutableMapOf(Pair("X", mockedVarX)),
      ),
      Arguments.of(
          "Successful creation of a linear variable",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("Y", -1.0, 10.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_CONTINUOUS)).thenReturn(mockedVarY)
            return mockedModel
          },
          listOf( LPVar("Y", LPVarType.DOUBLE, -1.0, 10.0)),
          true,
          mutableMapOf(Pair("Y", mockedVarY)),
      ),
      Arguments.of(
          "Successful creation of an integer variable",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("Z", -1.3, 20.2, 0.0, SCIP_Vartype.SCIP_VARTYPE_INTEGER)).thenReturn(mockedVarZ)
            return mockedModel
          },
          listOf( LPVar("Z", LPVarType.INTEGER, -1.3, 20.2)),
          true,
          mutableMapOf(Pair("Z", mockedVarZ)),
      ),
      Arguments.of(
          "Successful creation of multiple variables",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.createVar("Y", -1.0, 10.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_CONTINUOUS)).thenReturn(mockedVarY)
            `when`(mockedModel.createVar("Z", -1.3, 20.2, 0.0, SCIP_Vartype.SCIP_VARTYPE_INTEGER)).thenReturn(mockedVarZ)
            return mockedModel
          },
          listOf(
              LPVar("X", LPVarType.BOOLEAN),
              LPVar("Y", LPVarType.DOUBLE, -1.0, 10.0),
              LPVar("Z", LPVarType.INTEGER, -1.3, 20.2)),
          true,
          mutableMapOf(
              Pair("X", mockedVarX),
              Pair("Y", mockedVarY),
              Pair("Z", mockedVarZ)),
      ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitVars")
  fun testInitVars(
      desc: String,
      initMock: () -> Scip,
      lpVars: List<LPVar>,
      wantSuccess: Boolean,
      wantVarMap: Map<String, Variable>
  ) {
    log.info { "Test Case $desc" }
    val mockedScipModel = initMock()
    val model = LPModel("test")
    lpVars.forEach { lpVar ->  model.variables.add(lpVar) }
    val solver = ScipLpSolver(model)
    setScipModel(solver, mockedScipModel)
    solver.initModel()
    val gotVarMap = mutableMapOf<String, Variable>().apply {
      setVariableMap(solver, this)
    }
    val gotSuccess = solver.initVars()
    assertEquals(wantSuccess, gotSuccess, "solver.initVars()")
    assertEquals(gotVarMap, wantVarMap, "solver.variableMap")
  }


  private fun argsForInitConstraints() = Stream.of(
      Arguments.of(
          "Exception while initializing a constraint",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.createConsLinear("constraint1", arrayOf(mockedVarX), doubleArrayOf(1.0), 1.0, 1.0)).thenThrow(RuntimeException())
            `when`(mockedModel.addCons(mockedConstraint1)).then { createdConstraints.add(mockedConstraint1)}
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
              Pair("X", mockedVarX)
          ),
          mutableMapOf<String, Constraint>(),
      ),
      Arguments.of(
          "Exception while adding Constraint",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.createConsLinear("constraint1", arrayOf(mockedVarX), doubleArrayOf(1.0), 1.0, 1.0)).thenReturn(mockedConstraint1)
            `when`(mockedModel.addCons(mockedConstraint1)).thenThrow(RuntimeException())
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
              Pair("X", mockedVarX)
          ),
          mutableMapOf<String, Constraint>(),
      ),
      Arguments.of(
          "Successful initialization of a single equality constraint",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_INTEGER)).thenReturn(mockedVarY)
            `when`(mockedModel.createConsLinear("constraint1", arrayOf(mockedVarX, mockedVarY), doubleArrayOf(1.0, -2.0), 1.0, 1.0)).thenReturn(mockedConstraint1)
            `when`(mockedModel.addCons(mockedConstraint1)).then { createdConstraints.add(mockedConstraint1)}
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
              Pair("Y", mockedVarY)
          ),
          mutableMapOf(Pair("constraint1", mockedConstraint1)),
      ),
      Arguments.of(
          "Successful initialization of a <= constraint (2x + 3y <= 5)",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
            `when`(mockedModel.createVar("Z", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_CONTINUOUS)).thenReturn(mockedVarZ)
            `when`(mockedModel.infinity()).thenReturn(SCIP_INF)
            `when`(mockedModel.createConsLinear("constraint1", arrayOf(mockedVarX, mockedVarY), doubleArrayOf(2.0, 3.0), -1.0 * SCIP_INF, 5.0)).thenReturn(mockedConstraint1)
            `when`(mockedModel.addCons(mockedConstraint1)).then { createdConstraints.add(mockedConstraint1)}
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
              Pair("Z", mockedVarZ)
          ),
          mutableMapOf(Pair("constraint1", mockedConstraint1)),
      ),
      Arguments.of(
          "Successful initialization of a >= constraint (3y + 4x >= 3)",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
            `when`(mockedModel.createVar("Z", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_CONTINUOUS)).thenReturn(mockedVarZ)
            `when`(mockedModel.infinity()).thenReturn(SCIP_INF)
            `when`(mockedModel.createConsLinear("constraint1", arrayOf(mockedVarY, mockedVarX), doubleArrayOf(3.0, 4.0), 3.0,  SCIP_INF)).thenReturn(mockedConstraint1)
            `when`(mockedModel.addCons(mockedConstraint1)).then { createdConstraints.add(mockedConstraint1)}
            return mockedModel
          },
          fun(): LPModel {
            val model = LPModel("test")
            model.variables.add(LPVar("X", LPVarType.BOOLEAN))
            model.variables.add(LPVar("Y", LPVarType.BOOLEAN, 0.0, 1.0))
            model.variables.add(LPVar("Z", LPVarType.DOUBLE, 0, 1))
            val constr = LPConstraint("constraint1")
            constr.lhs.addTerm(2, "Y").addTerm(2, "X").add(-1)
            constr.operator = LPOperator.GREATER_EQUAL
            constr.rhs.addTerm(-1.0, "Y").addTerm(-2.0, "X").add(2)
            model.constraints.add(constr)
            return model
          },
          true,
          mutableMapOf(
              Pair("X", mockedVarX),
              Pair("Y", mockedVarY),
              Pair("Z", mockedVarZ)
          ),
          mutableMapOf(Pair("constraint1", mockedConstraint1)),
      ),
      Arguments.of(
          "Successful initialization of multiple constraints (1) aX + by + cZ <= 4 (2) X + Y >= 2",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
            `when`(mockedModel.createVar("Z", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarZ)
            `when`(mockedModel.infinity()).thenReturn(SCIP_INF)
            `when`(mockedModel.createConsLinear("constraint1", arrayOf(mockedVarX, mockedVarY, mockedVarZ), doubleArrayOf(2.0, 3.0, 4.0), -1.0 * SCIP_INF, 4.0)).thenReturn(mockedConstraint1)
            `when`(mockedModel.createConsLinear("constraint2", arrayOf(mockedVarX, mockedVarY), doubleArrayOf(1.0, 1.0), 2.0, SCIP_INF)).thenReturn(mockedConstraint2)
            `when`(mockedModel.addCons(mockedConstraint1)).then { createdConstraints.add(mockedConstraint1)}
            `when`(mockedModel.addCons(mockedConstraint1)).then { createdConstraints.add(mockedConstraint2)}
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
            constr1.lhs.addTerm("a", "X").addTerm("b", "Y").addTerm("c", "Z").add(-4)
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
              Pair("Z", mockedVarZ)
          ),
          mutableMapOf(
              Pair("constraint1", mockedConstraint1),
              Pair("constraint2", mockedConstraint2)),
      ),
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitConstraints")
  fun testInitConstraints(
      desc: String,
      initMock: () -> Scip,
      initModel: () -> LPModel,
      wantSuccess: Boolean,
      wantVarMap: Map<String, Variable>,
      wantConstraintMap: Map<String, Constraint>
  ) {
    log.info { "Test Case $desc" }
    createdConstraints.clear()
    val mockedScipModel = initMock()
    val model = initModel()
    val solver = ScipLpSolver(model)
    setScipModel(solver, mockedScipModel)
    solver.initModel()
    val gotVarMap = mutableMapOf<String, Variable>().apply {
      setVariableMap(solver, this)
    }
    val gotConstraintMap = mutableMapOf<String, Constraint>().apply {
      setConstraintMap(solver, this)
    }
    val varSuccess = solver.initVars()
    assertEquals(varSuccess, true, "solver.initVars")
    val gotSuccess = solver.initConstraints()
    assertEquals(wantSuccess, gotSuccess, "solver.initConstraints()")
    assertEquals(gotVarMap, wantVarMap, "solver.variableMap")
    assertEquals(gotConstraintMap, wantConstraintMap, "solver.constraintMap")
    assertEquals(createdConstraints, wantConstraintMap.values.toSet(), "solver.create calls")
  }

  private fun argsForInitObjective() = Stream.of(
      Arguments.of(
          "Exception while initializing the objective function",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.changeVarObj(mockedVarX, 1.0)).thenThrow(RuntimeException())
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
              Pair("X", mockedVarX)
          ),
          mutableMapOf<Variable, Number>()
      ),
      Arguments.of(
          "Exception while initializing the objective direction",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.changeVarObj(mockedVarX, 1.0)).then{ createdObjectiveCoefficients.put(mockedVarX, 1.0) }
            `when`(mockedModel.setMaximize()).thenThrow(RuntimeException())
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
              Pair("X", mockedVarX)
          ),
          mutableMapOf<Variable, Number>(Pair(mockedVarX, 1.0))
      ),
      Arguments.of(
          "Reduced expression with maximization objective  5x + 2y -2x + 2y",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
            `when`(mockedModel.changeVarObj(mockedVarX, 3.0)).then{ createdObjectiveCoefficients.put(mockedVarX, 3.0) }
            `when`(mockedModel.changeVarObj(mockedVarY, 4.0)).then{ createdObjectiveCoefficients.put(mockedVarY, 4.0) }
            // Throw exception on invalid call
            `when`(mockedModel.setMinimize()).thenThrow(RuntimeException())
            return mockedModel
          },
          fun(): LPModel {
            val model = LPModel("test")
            model.variables.add(LPVar("X", LPVarType.BOOLEAN))
            model.variables.add(LPVar("Y", LPVarType.BOOLEAN))
            model.objective.expression.addTerm(5, "X").addTerm(2, "Y").addTerm(-2, "X").addTerm(2, "Y")
            model.objective.objective = LPObjectiveType.MAXIMIZE
            return model
          },
          true,
          mutableMapOf(
              Pair("X", mockedVarX),
              Pair("Y", mockedVarY)
          ),
          mutableMapOf<Variable, Number>(
              Pair(mockedVarX, 3.0),
              Pair(mockedVarY, 4.0),
          )
      ),
      Arguments.of(
          "Reduced expression with minimization objective  5x + 2y -2x - 2y",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
            `when`(mockedModel.changeVarObj(mockedVarX, 3.0)).then{ createdObjectiveCoefficients.put(mockedVarX, 3.0) }
            // Throw exception on invalid call
            `when`(mockedModel.setMaximize()).thenThrow(RuntimeException())
            return mockedModel
          },
          fun(): LPModel {
            val model = LPModel("test")
            model.variables.add(LPVar("X", LPVarType.BOOLEAN))
            model.variables.add(LPVar("Y", LPVarType.BOOLEAN))
            model.objective.expression.addTerm(5, "X").addTerm(2, "Y").addTerm(-2, "X").addTerm(-2, "Y")
            model.objective.objective = LPObjectiveType.MINIMIZE
            return model
          },
          true,
          mutableMapOf(
              Pair("X", mockedVarX),
              Pair("Y", mockedVarY)
          ),
          mutableMapOf<Variable, Number>(
              Pair(mockedVarX, 3.0),
          )
      ),
      Arguments.of(
          "Empty Objective function (feasibility problem)",
          fun(): Scip {
            val mockedModel = mock<Scip>{}
            `when`(mockedModel.createVar("X", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarX)
            `when`(mockedModel.createVar("Y", 0.0, 1.0, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY)).thenReturn(mockedVarY)
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
              Pair("Y", mockedVarY)
          ),
          mutableMapOf<Variable, Number>()
      )
  )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitObjective")
  fun testInitObjective(
      desc: String,
      initMock: () -> Scip,
      initModel: () -> LPModel,
      wantSuccess: Boolean,
      wantVarMap: Map<String, Variable>,
      wantObjectiveCoefficients: Map<Variable, Number>
  ) {
    log.info { "Test Case $desc" }
    createdObjectiveCoefficients.clear()
    val mockedScipModel = initMock()
    val model = initModel()
    val solver = ScipLpSolver(model)
    setScipModel(solver, mockedScipModel)
    solver.initModel()
    val gotVarMap = mutableMapOf<String, Variable>().apply {
      setVariableMap(solver, this)
    }
    val varSuccess = solver.initVars()
    assertEquals(varSuccess, true, "solver.initVars")
    val gotSuccess = solver.initObjectiveFunction()
    assertEquals(wantSuccess, gotSuccess, "solver.initObjectiveFunction()")
    assertEquals(gotVarMap, wantVarMap, "solver.variableMap")
    assertEquals(createdObjectiveCoefficients, wantObjectiveCoefficients, "solver.changeObjVal calls")
  }
}

fun testExtractResults() {

}