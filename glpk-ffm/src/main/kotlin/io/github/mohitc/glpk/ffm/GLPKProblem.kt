package io.github.mohitc.glpk.ffm

import org.glpk.java.GLPK
import org.glpk.java.glp_iocp
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.util.concurrent.atomic.AtomicBoolean

class GLPKProblem : AutoCloseable {
  private val glpPtr: MemorySegment
  private val isClosed: AtomicBoolean

  constructor() {
    this.glpPtr = GLPK.glp_create_prob()
    this.isClosed = AtomicBoolean(false)
    GLPK.glp_create_index(glpPtr)
  }

  private fun checkOpen() {
    if (isClosed.get()) {
      throw RuntimeException("GLPKProblem is closed")
    }
  }

  override fun close() {
    if (isClosed.compareAndSet(false, true)) {
      GLPK.glp_delete_prob(glpPtr)
    }
  }

  fun setModelName(modelName: String) {
    checkOpen()
    Arena.ofConfined().use { GLPK.glp_set_prob_name(glpPtr, it.allocateFrom(modelName)) }
  }

  fun getModelName(): String {
    checkOpen()
    return GLPK.glp_get_prob_name(glpPtr).getString(0)
  }

  fun getObjectiveName(): String {
    checkOpen()
    return GLPK.glp_get_obj_name(glpPtr).getString(0)
  }

  fun setObjectiveName(objName: String) {
    checkOpen()
    Arena.ofConfined().use { GLPK.glp_set_obj_name(glpPtr, it.allocateFrom(objName)) }
  }

  fun getObjective(): GLPKObjective? {
    checkOpen()
    val objDir = GLPK.glp_get_obj_dir(glpPtr)
    return GLPKObjective.entries.firstOrNull { it.value == objDir }
  }

  fun setObjective(direction: GLPKObjective) {
    checkOpen()
    GLPK.glp_set_obj_dir(glpPtr, direction.value)
  }

  fun addRows(rowCount: Int): Int {
    checkOpen()
    return GLPK.glp_add_rows(glpPtr, rowCount)
  }

  fun addCols(colCount: Int): Int {
    checkOpen()
    return GLPK.glp_add_cols(glpPtr, colCount)
  }

  fun setRowName(
    rowIndex: Int,
    name: String,
  ) {
    checkOpen()
    Arena.ofConfined().use { GLPK.glp_set_row_name(glpPtr, rowIndex, it.allocateFrom(name)) }
  }

  fun setColName(
    colIndex: Int,
    name: String,
  ) {
    checkOpen()
    Arena.ofConfined().use { GLPK.glp_set_col_name(glpPtr, colIndex, it.allocateFrom(name)) }
  }

  fun setRowBounds(
    rowIndex: Int,
    boundType: GLPKBoundType,
    lb: Double,
    ub: Double,
  ) {
    checkOpen()
    GLPK.glp_set_row_bnds(glpPtr, rowIndex, boundType.value, lb, ub)
  }

  fun setColBounds(
    colIndex: Int,
    boundType: GLPKBoundType,
    lb: Double,
    ub: Double,
  ) {
    checkOpen()
    GLPK.glp_set_col_bnds(glpPtr, colIndex, boundType.value, lb, ub)
  }

  fun setObjectiveCoefficient(
    colIndex: Int,
    coefficient: Double,
  ) {
    checkOpen()
    GLPK.glp_set_obj_coef(glpPtr, colIndex, coefficient)
  }

  fun setMatrixRow(
    rowIndex: Int,
    length: Int,
    indexes: List<Int>,
    values: List<Double>,
  ) {
    checkOpen()
    Arena.ofConfined().use {
      GLPK.glp_set_mat_row(
        glpPtr,
        rowIndex,
        length,
        it.allocateFrom(ValueLayout.JAVA_INT, 0, *indexes.toIntArray()),
        it.allocateFrom(ValueLayout.JAVA_DOUBLE, 0.0, *values.toDoubleArray()),
      )
    }
  }

  fun getNumRows(): Int {
    checkOpen()
    return GLPK.glp_get_num_rows(glpPtr)
  }

  fun getNumCols(): Int {
    checkOpen()
    return GLPK.glp_get_num_cols(glpPtr)
  }

  fun getRowType(rowIndex: Int): GLPKBoundType? {
    checkOpen()
    val rowType = GLPK.glp_get_row_type(glpPtr, rowIndex)
    return GLPKBoundType.entries.firstOrNull { it.value == rowType }
  }

  fun getRowLowerBound(rowIndex: Int): Double {
    checkOpen()
    return GLPK.glp_get_row_lb(glpPtr, rowIndex)
  }

  fun getRowUpperBound(rowIndex: Int): Double {
    checkOpen()
    return GLPK.glp_get_row_ub(glpPtr, rowIndex)
  }

  fun getColType(colIndex: Int): GLPKBoundType? {
    checkOpen()
    val colType = GLPK.glp_get_col_type(glpPtr, colIndex)
    return GLPKBoundType.entries.firstOrNull { it.value == colType }
  }

  fun getColLowerBound(colIndex: Int): Double {
    checkOpen()
    return GLPK.glp_get_col_lb(glpPtr, colIndex)
  }

  fun getColUpperBound(colIndex: Int): Double {
    checkOpen()
    return GLPK.glp_get_col_ub(glpPtr, colIndex)
  }

  fun findRow(rowName: String): Int {
    checkOpen()
    return Arena.ofConfined().use { GLPK.glp_find_row(glpPtr, it.allocateFrom(rowName)) }
  }

  fun findCol(colName: String): Int {
    checkOpen()
    return Arena.ofConfined().use { GLPK.glp_find_col(glpPtr, it.allocateFrom(colName)) }
  }

  fun getObjectiveValue(): Double {
    checkOpen()
    return GLPK.glp_get_obj_val(glpPtr)
  }

  fun setColKind(
    colIndex: Int,
    varType: GLPKVarKind,
  ) {
    checkOpen()
    GLPK.glp_set_col_kind(glpPtr, colIndex, varType.value)
  }

  fun getColKind(colIndex: Int): GLPKVarKind? {
    checkOpen()
    val varKind = GLPK.glp_get_col_kind(glpPtr, colIndex)
    return GLPKVarKind.entries.firstOrNull { it.value == varKind }
  }

  fun intopt(params: GlpIocp): Int {
    checkOpen()
    Arena.ofConfined().use {
      val cStruct = glp_iocp.allocate(it)
      GLPK.glp_init_iocp(cStruct)
      params.apply(cStruct)
      return GLPK.glp_intopt(glpPtr, cStruct)
    }
  }

  fun mipStatus(): GLPKMipStatus? {
    checkOpen()
    val mipStatus = GLPK.glp_mip_status(glpPtr)
    return GLPKMipStatus.entries.firstOrNull { it.value == mipStatus }
  }

  fun mipObjectiveValue(): Double {
    checkOpen()
    return GLPK.glp_mip_obj_val(glpPtr)
  }

  fun mipRowVal(rowIndex: Int): Double {
    checkOpen()
    return GLPK.glp_mip_row_val(glpPtr, rowIndex)
  }

  fun mipColVal(colIndex: Int): Double {
    checkOpen()
    return GLPK.glp_mip_col_val(glpPtr, colIndex)
  }
}