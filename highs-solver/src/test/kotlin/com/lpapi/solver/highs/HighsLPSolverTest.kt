package com.lpapi.solver.highs

import com.lpapi.ffm.highs.HIGHSObjective
import com.lpapi.ffm.highs.HIGHSProblem
import com.lpapi.ffm.highs.HIGHSVarType
import io.github.mohitc.lpapi.model.LPConstant
import io.github.mohitc.lpapi.model.LPConstraint
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPVar
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HighsLPSolverTest {
  private val log = KotlinLogging.logger { this.javaClass.name }

  companion object {
    val VarX = 1
    val VarY = 2
    val VarZ = 3
    val Constraint1 = 10
    val Constraint2 = 3
    val createdObjectiveCoefficients = mutableMapOf<Int, Number>()
    val constantObjectiveTerm = -1
    const val HIGHS_INF = 1E20
  }

  private fun setParameter(
    solver: HighsLPSolver,
    field: String,
    value: Any?,
  ) {
    solver.javaClass.getDeclaredField(field).let {
      it.isAccessible = true
      it.set(solver, value)
    }
  }

  private fun setVariableMap(
    solver: HighsLPSolver,
    varMap: MutableMap<String, Int>,
  ) {
    setParameter(solver, "variableMap", varMap)
  }

  private fun setConstraintMap(
    solver: HighsLPSolver,
    constraintMap: MutableMap<String, Int>,
  ) {
    setParameter(solver, "constraintMap", constraintMap)
  }

  private fun setHighsModel(
    solver: HighsLPSolver,
    model: HIGHSProblem,
  ) {
    setParameter(solver, "highsModel", model)
  }

  @Test
  fun testHighsVarTypeComputation() {
    val model = HighsLPSolver(LPModel())
    LPVarType.values().forEach { lpVarType ->
      assertNotNull(model.getHighsVarType(lpVarType), "model.getHighsVarType($lpVarType) want not null got null")
    }
  }

  private fun argsForInitVars() =
    Stream.of(
      Arguments.of(
        "Exception while creating a boolean variable",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(
            mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER),
          ).thenThrow(RuntimeException())
          return mockedModel
        },
        listOf(LPVar("X", LPVarType.BOOLEAN)),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Invalid status code (null value) while creating a variable",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(
            mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER),
          ).thenReturn(null)
          return mockedModel
        },
        listOf(LPVar("X", LPVarType.BOOLEAN)),
        false,
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Successful creation of a boolean variable",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          return mockedModel
        },
        listOf(LPVar("X", LPVarType.BOOLEAN)),
        true,
        mutableMapOf(Pair("X", VarX)),
      ),
      Arguments.of(
        "Successful creation of a linear variable",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(
            mockedModel.createVar("Y", -1.0, 10.0, HIGHSVarType.CONTINUOUS),
          ).thenReturn(VarY)
          return mockedModel
        },
        listOf(LPVar("Y", LPVarType.DOUBLE, -1.0, 10.0)),
        true,
        mutableMapOf(Pair("Y", VarY)),
      ),
      Arguments.of(
        "Successful creation of an integer variable",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("Z", -1.3, 20.2, HIGHSVarType.INTEGER)).thenReturn(VarZ)
          return mockedModel
        },
        listOf(LPVar("Z", LPVarType.INTEGER, -1.3, 20.2)),
        true,
        mutableMapOf(Pair("Z", VarZ)),
      ),
      Arguments.of(
        "Successful creation of multiple variables",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(
            mockedModel.createVar("Y", -1.0, 10.0, HIGHSVarType.CONTINUOUS),
          ).thenReturn(VarY)
          `when`(mockedModel.createVar("Z", -1.3, 20.2, HIGHSVarType.INTEGER)).thenReturn(VarZ)
          return mockedModel
        },
        listOf(
          LPVar("X", LPVarType.BOOLEAN),
          LPVar("Y", LPVarType.DOUBLE, -1.0, 10.0),
          LPVar("Z", LPVarType.INTEGER, -1.3, 20.2),
        ),
        true,
        mutableMapOf(
          Pair("X", VarX),
          Pair("Y", VarY),
          Pair("Z", VarZ),
        ),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitVars")
  fun testInitVars(
    desc: String,
    initMock: () -> HIGHSProblem,
    lpVars: List<LPVar>,
    wantSuccess: Boolean,
    wantVarMap: Map<String, Int>,
  ) {
    log.info { "Test Case $desc" }
    val mockedHighsModel = initMock()
    val model = LPModel("test")
    lpVars.forEach { lpVar -> model.variables.add(lpVar) }
    val solver = HighsLPSolver(model)
    setHighsModel(solver, mockedHighsModel)
    solver.initModel()
    val gotVarMap =
      mutableMapOf<String, Int>().apply {
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
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(
            mockedModel.createConstraint("constraint1", 1.0, 1.0, listOf(Pair(VarX, 1.0))),
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
          Pair("X", VarX),
        ),
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Null value while adding Constraint",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(
            mockedModel.createConstraint("constraint1", 1.0, 1.0, listOf(Pair(VarX, 1.0))),
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
          Pair("X", VarX),
        ),
        mutableMapOf<String, Int>(),
      ),
      Arguments.of(
        "Successful initialization of a single equality constraint",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarY)
          `when`(
            mockedModel.createConstraint("constraint1", 1.0, 1.0, listOf(Pair(VarX, 1.0), Pair(VarY, -2.0))),
          ).thenReturn(Constraint1)
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
          Pair("X", VarX),
          Pair("Y", VarY),
        ),
        mutableMapOf(Pair("constraint1", Constraint1)),
      ),
      Arguments.of(
        "Successful initialization of a <= constraint (2x + 3y <= 5)",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarY)
          `when`(
            mockedModel.createVar("Z", 0.0, 1.0, HIGHSVarType.CONTINUOUS),
          ).thenReturn(VarZ)
          `when`(mockedModel.infinity()).thenReturn(HIGHS_INF)
          `when`(mockedModel.negInfinity()).thenReturn(HIGHS_INF * -1)
          `when`(
            mockedModel.createConstraint(
              "constraint1",
              -1.0 * HIGHS_INF,
              5.0,
              listOf(
                Pair(VarX, 2.0),
                Pair(VarY, 3.0),
              ),
            ),
          ).thenReturn(Constraint1)
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
          Pair("X", VarX),
          Pair("Y", VarY),
          Pair("Z", VarZ),
        ),
        mutableMapOf(Pair("constraint1", Constraint1)),
      ),
      Arguments.of(
        "Successful initialization of a >= constraint (3y + 4x >= 3)",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarY)
          `when`(
            mockedModel.createVar("Z", 0.0, 1.0, HIGHSVarType.CONTINUOUS),
          ).thenReturn(VarZ)
          `when`(mockedModel.infinity()).thenReturn(HIGHS_INF)
          `when`(mockedModel.negInfinity()).thenReturn(HIGHS_INF * -1)
          `when`(
            mockedModel.createConstraint(
              "constraint1",
              3.0,
              HIGHS_INF,
              listOf(Pair(VarY, 3.0), Pair(VarX, 4.0)),
            ),
          ).thenReturn(Constraint1)
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
          Pair("X", VarX),
          Pair("Y", VarY),
          Pair("Z", VarZ),
        ),
        mutableMapOf(Pair("constraint1", Constraint1)),
      ),
      Arguments.of(
        "Successful initialization of multiple constraints (1) aX + by + cZ <= 4 (2) X + Y >= 2",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarY)
          `when`(mockedModel.createVar("Z", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarZ)
          `when`(mockedModel.infinity()).thenReturn(HIGHS_INF)
          `when`(mockedModel.negInfinity()).thenReturn(HIGHS_INF * -1)
          `when`(
            mockedModel.createConstraint(
              "constraint1",
              -1.0 * HIGHS_INF,
              4.0,
              listOf(Pair(VarX, 2.0), Pair(VarY, 3.0), Pair(VarZ, 4.0)),
            ),
          ).thenReturn(Constraint1)
          `when`(
            mockedModel.createConstraint(
              "constraint2",
              2.0,
              HIGHS_INF,
              listOf(Pair(VarX, 1.0), Pair(VarY, 1.0)),
            ),
          ).thenReturn(Constraint2)
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
          Pair("X", VarX),
          Pair("Y", VarY),
          Pair("Z", VarZ),
        ),
        mutableMapOf(
          Pair("constraint1", Constraint1),
          Pair("constraint2", Constraint2),
        ),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitConstraints")
  fun testInitConstraints(
    desc: String,
    initMock: () -> HIGHSProblem,
    initModel: () -> LPModel,
    wantSuccess: Boolean,
    wantVarMap: Map<String, Int>,
    wantConstraintMap: Map<String, Int>,
  ) {
    log.info { "Test Case $desc" }
    val mockedScipModel = initMock()
    val model = initModel()
    val solver = HighsLPSolver(model)
    setHighsModel(solver, mockedScipModel)
    solver.initModel()
    val gotVarMap =
      mutableMapOf<String, Int>().apply {
        setVariableMap(solver, this)
      }
    val gotConstraintMap =
      mutableMapOf<String, Int>().apply {
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
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(mockedModel.changeObjectiveCoefficient(VarX, 1.0)).thenThrow(RuntimeException())
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
          Pair("X", VarX),
        ),
        mutableMapOf<Int, Number>(),
      ),
      Arguments.of(
        "Exception while initializing the objective direction",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(
            mockedModel.changeObjectiveCoefficient(VarX, 1.0),
          ).then { createdObjectiveCoefficients.put(VarX, 1.0) }
          `when`(mockedModel.changeObjectiveDirection(HIGHSObjective.MAXIMIZE)).thenThrow(RuntimeException())
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
          Pair("X", VarX),
        ),
        mutableMapOf<Int, Number>(Pair(VarX, 1.0)),
      ),
      Arguments.of(
        "Reduced expression with maximization objective  5x + 2y -2x + 2y",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarY)
          `when`(
            mockedModel.changeObjectiveCoefficient(VarX, 3.0),
          ).then { createdObjectiveCoefficients.put(VarX, 3.0) }
          `when`(
            mockedModel.changeObjectiveCoefficient(VarY, 4.0),
          ).then { createdObjectiveCoefficients.put(VarY, 4.0) }
          // Throw exception on invalid call
          `when`(mockedModel.changeObjectiveDirection(HIGHSObjective.MINIMIZE)).thenThrow(RuntimeException())
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
          Pair("X", VarX),
          Pair("Y", VarY),
        ),
        mutableMapOf<Int, Number>(
          Pair(VarX, 3.0),
          Pair(VarY, 4.0),
        ),
      ),
      Arguments.of(
        "Reduced expression with minimization objective  5x + 2y -2x - 2y + 3.0",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarY)
          `when`(
            mockedModel.changeObjectiveCoefficient(VarX, 3.0),
          ).then { createdObjectiveCoefficients.put(VarX, 3.0) }
          // Throw exception on invalid call
          `when`(mockedModel.changeObjectiveDirection(HIGHSObjective.MAXIMIZE)).thenThrow(RuntimeException())
          `when`(mockedModel.changeObjectiveOffset(3.0)).then {
            createdObjectiveCoefficients.put(
              constantObjectiveTerm,
              3.0,
            )
          }
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
            .add(3.0)
          model.objective.objective = LPObjectiveType.MINIMIZE
          return model
        },
        true,
        mutableMapOf(
          Pair("X", VarX),
          Pair("Y", VarY),
        ),
        mutableMapOf<Int, Number>(
          Pair(VarX, 3.0),
          Pair(constantObjectiveTerm, 3.0),
        ),
      ),
      Arguments.of(
        "Empty Objective function (feasibility problem)",
        fun(): HIGHSProblem {
          val mockedModel = mock<HIGHSProblem> {}
          `when`(mockedModel.createVar("X", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarX)
          `when`(mockedModel.createVar("Y", 0.0, 1.0, HIGHSVarType.INTEGER)).thenReturn(VarY)
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
          Pair("X", VarX),
          Pair("Y", VarY),
        ),
        mutableMapOf<Int, Number>(),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitObjective")
  fun testInitObjective(
    desc: String,
    initMock: () -> HIGHSProblem,
    initModel: () -> LPModel,
    wantSuccess: Boolean,
    wantVarMap: Map<String, Int>,
    wantObjectiveCoefficients: Map<Int, Number>,
  ) {
    log.info { "Test Case $desc" }
    createdObjectiveCoefficients.clear()
    val mockedScipModel = initMock()
    val model = initModel()
    val solver = HighsLPSolver(model)
    setHighsModel(solver, mockedScipModel)
    solver.initModel()
    val gotVarMap =
      mutableMapOf<String, Int>().apply {
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