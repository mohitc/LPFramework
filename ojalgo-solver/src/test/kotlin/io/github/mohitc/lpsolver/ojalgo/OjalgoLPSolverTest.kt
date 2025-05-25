package io.github.mohitc.lpsolver.ojalgo

import io.github.mohitc.lpapi.model.LPConstraint
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPVar
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.ojalgo.optimisation.ExpressionsBasedModel
import org.ojalgo.optimisation.Variable
import java.math.BigDecimal
import java.util.stream.Stream
import kotlin.math.abs

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OjalgoLPSolverTest {
  private val log = KotlinLogging.logger { this.javaClass.name }

  private fun setParameter(
    solver: OjalgoLpSolver,
    field: String,
    value: Any?,
  ) {
    solver.javaClass.getDeclaredField(field).let {
      it.isAccessible = true
      it.set(solver, value)
    }
  }

  private fun setModel(
    solver: OjalgoLpSolver,
    model: ExpressionsBasedModel?,
  ) = setParameter(solver, "ojalgoModel", model)

  private fun setVariableMap(
    solver: OjalgoLpSolver,
    variableMap: MutableMap<String, Variable>,
  ) = setParameter(solver, "variableMap", variableMap)

  private fun argsForInitVars() =
    Stream.of(
      Arguments.of(
        "Empty list of variables",
        listOf<LPVar>(),
      ),
      Arguments.of(
        "Multiple variables",
        listOf(
          LPVar("x", LPVarType.BOOLEAN),
          LPVar("y", LPVarType.INTEGER, -10, +21.3),
          LPVar("Z", LPVarType.DOUBLE, -22, -11),
        ),
      ),
    )

  private fun validateBoundAssertion(
    gotValue: BigDecimal,
    wantValue: Double,
    assertion: String,
  ) = assertTrue(abs(gotValue.toDouble() - wantValue) <= 0.01, "$assertion got $gotValue want $wantValue")

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitVars")
  fun testInitVars(
    desc: String,
    lpVars: List<LPVar>,
  ) {
    log.info { "Test case: $desc" }
    val lpModel = LPModel("test").apply { lpVars.forEach { lpVar -> this.variables.add(lpVar) } }
    val solver = OjalgoLpSolver(lpModel)
    val gotSuccess = solver.initVars()
    assertTrue(gotSuccess, "solver.initVars() want true got false")
    val baseModel = solver.getBaseModel()
    assertNotNull(baseModel, "solver.getBaseModel() want non null, got null")
    val gotVarMap = baseModel!!.variables.associateBy { it.name }
    assertTrue(gotVarMap.size == lpVars.size, "gotVarMap.size want ${lpVars.size} got ${gotVarMap.size}")
    lpVars.forEach { lpVar ->
      log.info { "Validating assertions for ${lpVar.identifier}" }
      val modelVar = gotVarMap[lpVar.identifier]
      assertNotNull(modelVar, "gotVarMap[${lpVar.identifier}] got null want non-null")
      validateBoundAssertion(modelVar!!.lowerLimit, lpVar.lbound, "modelVar.lowerLimit")
      validateBoundAssertion(modelVar.upperLimit, lpVar.ubound, "modelVar.upperLimit")
      assertTrue(
        modelVar.isInteger == (lpVar.type == LPVarType.INTEGER || lpVar.type == LPVarType.BOOLEAN),
        "modelVar type assertion failed isInteger = ${modelVar.isInteger} lpVar type ${lpVar.type}",
      )
    }
  }

  private fun argForInitVars_errors() =
    Stream.of(
      Arguments.of(
        "Exception while creating variable",
        fun(): ExpressionsBasedModel =
          mock<ExpressionsBasedModel> {}.apply {
            `when`(this.addVariable("x")).thenThrow(RuntimeException("error"))
          },
        LPVar("x", LPVarType.BOOLEAN),
      ),
      Arguments.of(
        "Null value while creating variable",
        fun(): ExpressionsBasedModel =
          mock<ExpressionsBasedModel> {}.apply {
            `when`(this.addVariable("x")).thenReturn(null)
          },
        LPVar("x", LPVarType.BOOLEAN),
      ),
      Arguments.of(
        "Exception while setting a lower bound",
        fun(): ExpressionsBasedModel {
          val mockedVar =
            mock<Variable> {}.apply {
              `when`(this.lower(0.0)).thenThrow(RuntimeException("exception"))
            }
          return mock<ExpressionsBasedModel> {}.apply {
            `when`(this.addVariable("x")).thenReturn(mockedVar)
          }
        },
        LPVar("x", LPVarType.BOOLEAN),
      ),
      Arguments.of(
        "Exception while setting upper bound",
        fun(): ExpressionsBasedModel {
          val mockedVar =
            mock<Variable> {}.apply {
              `when`(this.upper(1.0)).thenThrow(RuntimeException("exception"))
            }
          return mock<ExpressionsBasedModel> {}.apply {
            `when`(this.addVariable("x")).thenReturn(mockedVar)
          }
        },
        LPVar("x", LPVarType.BOOLEAN),
      ),
      Arguments.of(
        "Exception while setting variable type",
        fun(): ExpressionsBasedModel {
          val mockedVar =
            mock<Variable> {}.apply {
              `when`(this.integer(true)).thenThrow(RuntimeException("exception"))
            }
          return mock<ExpressionsBasedModel> {}.apply {
            `when`(this.addVariable("x")).thenReturn(mockedVar)
          }
        },
        LPVar("x", LPVarType.BOOLEAN),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argForInitVars_errors")
  fun testInitVars(
    desc: String,
    modelInit: () -> ExpressionsBasedModel,
    lpVar: LPVar,
  ) {
    log.info("Test case: $desc")
    val lpModel = LPModel("test").apply { this.variables.add(lpVar) }
    val gotVariableMap = mutableMapOf<String, Variable>()
    val solver =
      OjalgoLpSolver(lpModel).apply {
        setModel(this, modelInit())
        setVariableMap(this, gotVariableMap)
      }
    val status = solver.initVars()
    assertFalse(status, "solver.initVars() want false got true")
    assertTrue(gotVariableMap.isEmpty(), "Expect no variables in model, got $gotVariableMap")
  }

  fun argsForInitConstraints() =
    Stream.of(
      Arguments.of(
        "Equality constraint",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.BOOLEAN))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
        },
        LPConstraint("testConstraint").apply {
          this.lhs.addTerm(2, "x").addTerm(3, "y")
          this.rhs.add(4)
          this.operator = LPOperator.EQUAL
        },
        mapOf(Pair("x", 2.0), Pair("y", 3.0)),
        4.0,
      ),
      Arguments.of(
        "Less equal constraint",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
        },
        LPConstraint("testConstraint").apply {
          this.lhs
            .addTerm(3, "x")
            .addTerm(5, "y")
            .add(-4)
          this.rhs
            .add(4)
            .addTerm(2, "x")
            .addTerm(-3, "y")
          this.operator = LPOperator.LESS_EQUAL
        },
        mapOf(Pair("x", 1.0), Pair("y", 8.0)),
        8.0,
      ),
      Arguments.of(
        "Greater equal constraint",
        LPModel("test").apply {
          this.variables.add(LPVar("x", LPVarType.INTEGER))
          this.variables.add(LPVar("y", LPVarType.BOOLEAN))
        },
        LPConstraint("testConstraint").apply {
          this.lhs
            .addTerm(3, "x")
            .addTerm(5, "y")
            .add(-4)
          this.rhs
            .add(8)
            .addTerm(3, "x")
            .addTerm(-3, "y")
          this.operator = LPOperator.LESS_EQUAL
        },
        mapOf(Pair("y", 8.0)),
        12.0,
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitConstraints")
  fun testInitConstraints(
    desc: String,
    model: LPModel,
    constraint: LPConstraint,
    wantVarCoefficients: Map<String, Double>,
    rhs: Double,
  ) {
    log.info { "Test case: $desc" }
    model.constraints.add(constraint)
    val solver = OjalgoLpSolver(model)
    val varMap = mutableMapOf<String, Variable>()
    setVariableMap(solver, varMap)
    var status = solver.initVars()
    assertTrue(status, "solver.initVars() want true got false")
    status = solver.initConstraints()
    assertTrue(status, "solver.initConstraints() want true got false")
    val baseConstraint = solver.getBaseModel()!!.getExpression(constraint.identifier)
    assertNotNull(baseConstraint, "solver.getExpression(${constraint.identifier}) want non-null got null")
    assertTrue(baseConstraint.isConstraint, "isConstraint want True got False")
    // Validate operator
    when (constraint.operator) {
      LPOperator.EQUAL -> assertTrue(baseConstraint.isEqualityConstraint, "isEqualityConstraint want true got false")
      LPOperator.LESS_EQUAL -> assertTrue(baseConstraint.isUpperConstraint, "isUpperConstraint want true got false")
      LPOperator.GREATER_EQUAL -> assertTrue(baseConstraint.isLowerConstraint, "isLowerConstraint want true got false")
    }
    // validate variable coefficients
    wantVarCoefficients.entries.forEach { (k, v) ->
      val gotCoeff = baseConstraint.get(varMap[k])
      validateBoundAssertion(gotCoeff, v, "coefficient for variable $k: ")
    }
    // validate constant
    val limit =
      if (baseConstraint.upperLimit != null) {
        baseConstraint.upperLimit
      } else {
        baseConstraint.lowerLimit!!
      }
    validateBoundAssertion(limit, rhs, "constant (rhs) term in expression: ")
  }

  private fun argsForInitObjective() =
    Stream.of(
      Arguments.of(
        "Simple objective function",
        LPModel("test").apply {
          variables.add(LPVar("x", LPVarType.BOOLEAN))
          variables.add(LPVar("y", LPVarType.BOOLEAN))
          objective.objective = LPObjectiveType.MAXIMIZE
          objective.expression.addTerm(2, "x").addTerm(3, "y")
        },
        mapOf(Pair("x", 2), Pair("y", 3)),
      ),
      Arguments.of(
        "Simple objective function",
        LPModel("test").apply {
          variables.add(LPVar("x", LPVarType.BOOLEAN))
          variables.add(LPVar("y", LPVarType.BOOLEAN))
          objective.objective = LPObjectiveType.MAXIMIZE
          objective.expression
            .addTerm(2, "x")
            .addTerm(3, "y")
            .addTerm(-3, "x")
            .addTerm(10.3, "y")
        },
        mapOf(Pair("x", -1), Pair("y", 13.3)),
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForInitObjective")
  fun testInitObjective(
    desc: String,
    model: LPModel,
    wantCoeffs: Map<String, Double>,
  ) {
    log.info { "Test case: $desc" }
    val solver = OjalgoLpSolver(model)
    val varMap = mutableMapOf<String, Variable>()
    setVariableMap(solver, varMap)
    var status = solver.initVars()
    assertTrue(status, "solver.initVars() want true got false")
    status = solver.initObjectiveFunction()
    assertTrue(status, "solver.initObjective() want true got false")
    val baseModel = solver.getBaseModel()
    assertNotNull(baseModel, "solver.getBaseModel() want non-null got null")
    val baseObj = baseModel!!.objective()
    wantCoeffs.entries.forEach { (k, v) ->
      validateBoundAssertion(baseObj.get(varMap[k]), v, "coefficient for variable $k :")
    }
  }
}