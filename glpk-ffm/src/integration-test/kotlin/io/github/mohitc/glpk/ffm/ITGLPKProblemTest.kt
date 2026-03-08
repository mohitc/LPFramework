package io.github.mohitc.glpk.ffm

import io.github.mohitc.glpk.ffm.GlpIocp
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ITGLPKProblemTest {
  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  @Test
  fun testChangeObjectiveDirection() {
    GLPKObjective.entries.forEach { v ->
      GLPKProblem().use { glpkProblem ->
        glpkProblem.setObjective(v)
        assertEquals(
          v,
          glpkProblem.getObjective(),
          "Want Direction ${v.description}",
        )
      }
    }
  }

  @Test
  fun testModelName() {
    GLPKProblem().use { glpkProblem ->
      val modelNameToSet = "model-name-test"
      glpkProblem.setModelName(modelNameToSet)
      assertEquals(modelNameToSet, glpkProblem.getModelName())
    }
  }

  @Test
  fun testObjectiveName() {
    GLPKProblem().use { glpkProblem ->
      val objNameToSet = "obj-name-test"
      glpkProblem.setObjectiveName(objNameToSet)
      assertEquals(objNameToSet, glpkProblem.getObjectiveName())
    }
  }

  @Test
  fun testGetCols() {
    GLPKProblem().use { glpkProblem ->
      val numVarsToCreate = 5
      val colMap = mutableMapOf<String, Int>()
      for (i in 1..numVarsToCreate) {
        val colName = "column-$i"
        val colIndex = glpkProblem.addCols(1)
        glpkProblem.setColName(colIndex, colName)
        glpkProblem.setColKind(colIndex, GLPKVarKind.CONTINUOUS)
        glpkProblem.setColBounds(colIndex, GLPKBoundType.DOUBLE_BOUNDED, 0.0, 1.0)
        colMap[colName] = colIndex
      }
      colMap.forEach { (colName, colIndex) ->
        assertEquals(colIndex, glpkProblem.findCol(colName))
      }
      assertEquals(numVarsToCreate, glpkProblem.getNumCols())
    }
  }

  @Test
  fun testGetRows() {
    GLPKProblem().use { glpkProblem ->
      val numConstraintsToCreate = 5
      val rowMap = mutableMapOf<String, Int>()
      for (i in 1..numConstraintsToCreate) {
        val rowName = "constraint-$i"
        val rowIndex = glpkProblem.addRows(1)
        glpkProblem.setRowName(rowIndex, rowName)
        glpkProblem.setRowBounds(rowIndex, GLPKBoundType.DOUBLE_BOUNDED, 0.0, 1.0)
        rowMap[rowName] = rowIndex
      }
      rowMap.forEach { (rowName, rowIndex) ->
        assertEquals(rowIndex, glpkProblem.findRow(rowName))
      }
      assertEquals(numConstraintsToCreate, glpkProblem.getNumRows())
    }
  }

  @Test
  fun testColKind() {
    GLPKProblem().use { glpkProblem ->
      GLPKVarKind.entries.forEach { v ->
        val colIndex = glpkProblem.addCols(1)
        glpkProblem.setColKind(colIndex, v)
        assertEquals(v, glpkProblem.getColKind(colIndex))
      }
    }
  }

  @Test
  fun testRowType() {
    GLPKProblem().use { glpkProblem ->
      GLPKBoundType.entries.forEach { v ->
        val rowIndex = glpkProblem.addRows(1)
        glpkProblem.setRowBounds(rowIndex, v, 0.0, 1.0)
        assertEquals(v, glpkProblem.getRowType(rowIndex))
      }
    }
  }

  private fun argsForBounds() =
    Stream.of(
      Arguments.of(
        GLPKBoundType.DOUBLE_BOUNDED,
        -20.0,
        40.0,
        -20.0,
        40.0,
      ),
      Arguments.of(
        GLPKBoundType.FIXED,
        -30.0,
        -30.0,
        -30.0,
        -30.0,
      ),
      Arguments.of(
        GLPKBoundType.UPPER_BOUNDED,
        -10.0,
        20.0,
        -1 * Double.MAX_VALUE,
        20.0,
      ),
      Arguments.of(
        GLPKBoundType.LOWER_BOUNDED,
        -20.0,
        40.0,
        -20.0,
        Double.MAX_VALUE,
      ),
      Arguments.of(
        GLPKBoundType.UNBOUNDED,
        -20.0,
        40.0,
        -1 * Double.MAX_VALUE,
        Double.MAX_VALUE,
      ),
    )

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForBounds")
  fun testColBounds(
    bound: GLPKBoundType,
    lb: Double,
    ub: Double,
    expectedLb: Double,
    expectedUb: Double,
  ) {
    GLPKProblem().use { glpkProblem ->
      log.info("Validating Column Bounds: ${bound.description}")
      val colIndex = glpkProblem.addCols(1)
      glpkProblem.setColBounds(colIndex, bound, lb, ub)
      assertEquals(bound, glpkProblem.getColType(colIndex), "Bound Type")
      assertEquals(expectedLb, glpkProblem.getColLowerBound(colIndex), "Lower Bound")
      assertEquals(expectedUb, glpkProblem.getColUpperBound(colIndex), "Upper Bound")
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("argsForBounds")
  fun testRowBounds(
    bound: GLPKBoundType,
    lb: Double,
    ub: Double,
    expectedLb: Double,
    expectedUb: Double,
  ) {
    GLPKProblem().use { glpkProblem ->
      log.info("Validating Row Bounds: ${bound.description}")
      val rowIndex = glpkProblem.addRows(1)
      glpkProblem.setRowBounds(rowIndex, bound, lb, ub)
      assertEquals(bound, glpkProblem.getRowType(rowIndex), "Bound Type")
      assertEquals(expectedLb, glpkProblem.getRowLowerBound(rowIndex), "Lower Bound")
      assertEquals(expectedUb, glpkProblem.getRowUpperBound(rowIndex), "Upper Bound")
    }
  }

  @Test
  fun testSolve() {
    val params =
      GlpIocp(
        preSolve = GLPKFeatureStatus.ON,
        messageLevel = GLPKMessageLevel.MSG_ON,
        binarize = GLPKFeatureStatus.ON,
      )
    // Maximize x + y
    // s.t. x + y <= 1
    // x, y >= 0
    GLPKProblem().use { glpkProblem ->
      glpkProblem.setObjective(GLPKObjective.MAXIMIZE)

      val x = glpkProblem.addCols(1)
      glpkProblem.setColBounds(x, GLPKBoundType.LOWER_BOUNDED, 0.0, 0.0)
      glpkProblem.setColKind(x, GLPKVarKind.CONTINUOUS)
      glpkProblem.setObjectiveCoefficient(x, 1.0)

      val y = glpkProblem.addCols(1)
      glpkProblem.setColBounds(y, GLPKBoundType.LOWER_BOUNDED, 0.0, 0.0)
      glpkProblem.setColKind(y, GLPKVarKind.INTEGER)
      glpkProblem.setObjectiveCoefficient(y, 1.0)

      val row = glpkProblem.addRows(1)
      glpkProblem.setRowBounds(row, GLPKBoundType.UPPER_BOUNDED, 0.0, 1.0)
      glpkProblem.setMatrixRow(row, 2, listOf(x, y), listOf(1.0, 1.0))

      val ret = glpkProblem.intopt(params)
      assertEquals(0, ret, "Solver return code")
      assertEquals(GLPKMipStatus.OPTIMAL, glpkProblem.mipStatus())
      assertEquals(1.0, glpkProblem.mipObjectiveValue(), 0.0001)
    }
  }

  @Test
  fun testClose() {
    val problem = GLPKProblem()
    problem.close()
    // Should not throw exception on double close or similar if handled correctly
    problem.close()
  }
}