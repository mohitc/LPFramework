package io.github.mohitc.glpk.ffm

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
      val glpkProblem = GLPKProblem()
      glpkProblem.setObjective(v)
      assertEquals(
        v,
        glpkProblem.getObjective(),
        "Want Direction ${v.description}",
      )
      glpkProblem.cleanup()
    }
  }

  @Test
  fun testModelName() {
    val glpkProblem = GLPKProblem()
    try {
      val modelNameToSet = "model-name-test"
      glpkProblem.setModelName(modelNameToSet)
      assertEquals(modelNameToSet, glpkProblem.getModelName())
    } finally {
      glpkProblem.cleanup()
    }
  }

  @Test
  fun testObjectiveName() {
    val glpkProblem = GLPKProblem()
    try {
      val objNameToSet = "obj-name-test"
      glpkProblem.setObjectiveName(objNameToSet)
      assertEquals(objNameToSet, glpkProblem.getObjectiveName())
    } finally {
      glpkProblem.cleanup()
    }
  }

  @Test
  fun testGetCols() {
    val glpkProblem = GLPKProblem()
    try {
      glpkProblem.createIndex()
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
    } finally {
      glpkProblem.cleanup()
    }
  }

  @Test
  fun testGetRows() {
    val glpkProblem = GLPKProblem()
    try {
      glpkProblem.createIndex()
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
    } finally {
      glpkProblem.cleanup()
    }
  }

  @Test
  fun testColKind() {
    val glpkProblem = GLPKProblem()
    try {
      GLPKVarKind.entries.forEach { v ->
        val colIndex = glpkProblem.addCols(1)
        glpkProblem.setColKind(colIndex, v)
        assertEquals(v, glpkProblem.getColKind(colIndex))
      }
    } finally {
      glpkProblem.cleanup()
    }
  }

  @Test
  fun testRowType() {
    val glpkProblem = GLPKProblem()
    try {
      GLPKBoundType.entries.forEach { v ->
        val rowIndex = glpkProblem.addRows(1)
        glpkProblem.setRowBounds(rowIndex, v, 0.0, 1.0)
        assertEquals(v, glpkProblem.getRowType(rowIndex))
      }
    } finally {
      glpkProblem.cleanup()
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
    val glpkProblem = GLPKProblem()
    try {
      log.info("Validating Column Bounds: ${bound.description}")
      val colIndex = glpkProblem.addCols(1)
      glpkProblem.setColBounds(colIndex, bound, lb, ub)
      assertEquals(bound, glpkProblem.getColType(colIndex), "Bound Type")
      assertEquals(expectedLb, glpkProblem.getColLowerBound(colIndex), "Lower Bound")
      assertEquals(expectedUb, glpkProblem.getColUpperBound(colIndex), "Upper Bound")
    } finally {
      glpkProblem.cleanup()
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
    val glpkProblem = GLPKProblem()
    try {
      log.info("Validating Row Bounds: ${bound.description}")
      val rowIndex = glpkProblem.addRows(1)
      glpkProblem.setRowBounds(rowIndex, bound, lb, ub)
      assertEquals(bound, glpkProblem.getRowType(rowIndex), "Bound Type")
      assertEquals(expectedLb, glpkProblem.getRowLowerBound(rowIndex), "Lower Bound")
      assertEquals(expectedUb, glpkProblem.getRowUpperBound(rowIndex), "Upper Bound")
    } finally {
      glpkProblem.cleanup()
    }
  }
}