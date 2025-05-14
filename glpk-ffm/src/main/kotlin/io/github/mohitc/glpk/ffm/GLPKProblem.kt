package io.github.mohitc.glpk.ffm

import org.glpk.java.GLPK
import org.glpk.java.glp_iocp
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

class GLPKProblem {
  // Constant reference to a new GLPK problem instance
  private val glpPtr: MemorySegment = GLPK.glp_create_prob()

  fun setModelName(modelName: String) =
    Arena.ofConfined().use { GLPK.glp_set_prob_name(glpPtr, it.allocateFrom(modelName)) }

  fun getModelName(): String = GLPK.glp_get_prob_name(glpPtr).getString(0)

  fun getObjectiveName(): String = GLPK.glp_get_obj_name(glpPtr).getString(0)

  fun setObjectiveName(objName: String) =
    Arena.ofConfined().use { GLPK.glp_set_obj_name(glpPtr, it.allocateFrom(objName)) }

  fun getObjective(): GLPKObjective? {
    val objDir = GLPK.glp_get_obj_dir(glpPtr)
    return GLPKObjective.values().firstOrNull { it.value == objDir }
  }

  fun setObjective(direction: GLPKObjective) = GLPK.glp_set_obj_dir(glpPtr, direction.value)

  fun addRows(rowCount: Int) = GLPK.glp_add_rows(glpPtr, rowCount)

  fun addCols(colCount: Int) = GLPK.glp_add_cols(glpPtr, colCount)

  fun setRowName(
    rowIndex: Int,
    name: String,
  ) = Arena.ofConfined().use { GLPK.glp_set_row_name(glpPtr, rowIndex, it.allocateFrom(name)) }

  fun setColName(
    colIndex: Int,
    name: String,
  ) = Arena.ofConfined().use { GLPK.glp_set_col_name(glpPtr, colIndex, it.allocateFrom(name)) }

  fun setRowBounds(
    rowIndex: Int,
    boundType: GLPKBoundType,
    lb: Double,
    ub: Double,
  ) = GLPK.glp_set_row_bnds(glpPtr, rowIndex, boundType.value, lb, ub)

  fun setColBounds(
    colIndex: Int,
    boundType: GLPKBoundType,
    lb: Double,
    ub: Double,
  ) = GLPK.glp_set_col_bnds(glpPtr, colIndex, boundType.value, lb, ub)

  fun setObjectiveCoefficient(
    colIndex: Int,
    coefficient: Double,
  ) = GLPK.glp_set_obj_coef(glpPtr, colIndex, coefficient)

  fun setMatrixRow(
    rowIndex: Int,
    length: Int,
    indexes: List<Int>,
    values: List<Double>,
  ) = Arena.ofConfined().use {
    GLPK.glp_set_mat_row(
      glpPtr,
      rowIndex,
      length,
      it.allocateFrom(ValueLayout.JAVA_INT, 0, *indexes.toIntArray()),
      it.allocateFrom(ValueLayout.JAVA_DOUBLE, 0.0, *values.toDoubleArray()),
    )
  }

  fun getNumRows(): Int = GLPK.glp_get_num_rows(glpPtr)

  fun getNumCols(): Int = GLPK.glp_get_num_cols(glpPtr)

  fun getRowType(rowIndex: Int): GLPKBoundType? {
    val rowType = GLPK.glp_get_row_type(glpPtr, rowIndex)
    return GLPKBoundType.values().firstOrNull { it.value == rowType }
  }

  fun getRowLowerBound(rowIndex: Int) = GLPK.glp_get_row_lb(glpPtr, rowIndex)

  fun getRowUpperBound(rowIndex: Int) = GLPK.glp_get_row_ub(glpPtr, rowIndex)

  fun getColType(colIndex: Int): GLPKBoundType? {
    val colType = GLPK.glp_get_col_type(glpPtr, colIndex)
    return GLPKBoundType.values().firstOrNull { it.value == colType }
  }

  fun getColLowerBound(colIndex: Int) = GLPK.glp_get_col_lb(glpPtr, colIndex)

  fun getColUpperBound(colIndex: Int) = GLPK.glp_get_col_ub(glpPtr, colIndex)

  fun findRow(rowName: String): Int = Arena.ofConfined().use { GLPK.glp_find_row(glpPtr, it.allocateFrom(rowName)) }

  fun findCol(colName: String): Int = Arena.ofConfined().use { GLPK.glp_find_col(glpPtr, it.allocateFrom(colName)) }

  fun getObjectiveValue(): Double = GLPK.glp_get_obj_val(glpPtr)

  fun setColKind(
    colIndex: Int,
    varType: GLPKVarKind,
  ) = GLPK.glp_set_col_kind(glpPtr, colIndex, varType.value)

  fun getColKind(colIndex: Int): GLPKVarKind? {
    val varKind = GLPK.glp_get_col_kind(glpPtr, colIndex)
    return GLPKVarKind.values().firstOrNull { it.value == varKind }
  }

  fun intopt(params: GlpIocp): Int {
    Arena.ofConfined().use {
      val cStruct = glp_iocp.allocate(it)
      GLPK.glp_init_iocp(cStruct)
      params.apply(cStruct)
      return GLPK.glp_intopt(glpPtr, cStruct)
    }
  }

  fun mipStatus(): GLPKMipStatus? {
    val mipStatus = GLPK.glp_mip_status(glpPtr)
    return GLPKMipStatus.values().firstOrNull { it.value == mipStatus }
  }

  fun mipObjectiveValue(): Double = GLPK.glp_mip_obj_val(glpPtr)

  fun mipRowVal(rowIndex: Int): Double = GLPK.glp_mip_row_val(glpPtr, rowIndex)

  fun mipColVal(colIndex: Int): Double = GLPK.glp_mip_col_val(glpPtr, colIndex)
}