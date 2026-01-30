package io.github.mohitc.highs.ffm

import mu.KotlinLogging
import org.highs.java.HIGHS
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class HIGHSProblem : AutoCloseable {
  private val highsPtr: MemorySegment = HIGHS.Highs_create()
  private val isClosed = AtomicBoolean(false)

  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  private var varCtr: AtomicInteger = AtomicInteger(0)

  private var constraintCtr: AtomicInteger = AtomicInteger(0)

  private val maxStringLength: Long = 512 // mapped to kHighsMaximumStringLength

  private fun checkOpen() {
    if (isClosed.get()) {
      throw RuntimeException("HIGHSProblem is closed")
    }
  }

  override fun close() {
    if (isClosed.compareAndSet(false, true)) {
      HIGHS.Highs_destroy(highsPtr)
    }
  }

  fun setDoubleOptionValue(
    paramName: String,
    value: Double,
  ): HIGHSStatus {
    checkOpen()
    return Arena.ofConfined().use {
      HIGHSStatus.fromValue(HIGHS.Highs_setDoubleOptionValue(highsPtr, it.allocateFrom(paramName), value))
    }
  }

  fun passModelName(name: String): HIGHSStatus {
    checkOpen()
    Arena.ofConfined().use {
      return HIGHSStatus.fromValue(HIGHS.Highs_passModelName(highsPtr, it.allocateFrom(name)))
    }
  }

  fun createVar(
    name: String,
    lb: Double,
    ub: Double,
    varType: HIGHSVarType,
  ): Int? {
    checkOpen()
    Arena.ofConfined().use {
      var retCode = HIGHSStatus.fromValue(HIGHS.Highs_addVar(highsPtr, lb, ub))
      if (retCode != HIGHSStatus.OK) {
        log.error { "Highs_addVar() expected OK got $retCode for ($name, $lb, $ub, $varType)" }
        return null
      }
      val varIndex = varCtr.getAndIncrement()
      retCode = HIGHSStatus.fromValue(HIGHS.Highs_passColName(highsPtr, varIndex, it.allocateFrom(name)))
      if (retCode != HIGHSStatus.OK) {
        log.error { "Highs_passColName() expected OK got $retCode for ($name, $lb, $ub, $varType)" }
        return null
      }
      retCode = HIGHSStatus.fromValue(HIGHS.Highs_changeColIntegrality(highsPtr, varIndex, varType.value))
      if (retCode != HIGHSStatus.OK) {
        log.error { "Highs_changeColIntegrality() expected OK got $retCode for ($name, $lb, $ub, $varType)" }
        return null
      }
      return varIndex
    }
  }

  fun getVarByName(name: String): Int? {
    checkOpen()
    Arena.ofConfined().use {
      val colIndex = it.allocate(HIGHS.C_INT)
      val retCode = HIGHSStatus.fromValue(HIGHS.Highs_getColByName(highsPtr, it.allocateFrom(name), colIndex))
      if (retCode != HIGHSStatus.OK) {
        log.error { "Highs_getColByName($name) got return code $retCode" }
        return null
      }
      return colIndex.get(HIGHS.C_INT, 0)
    }
  }

  fun getVarName(colIndex: Int): String? {
    checkOpen()
    Arena.ofConfined().use {
      val strPtr = it.allocate(HIGHS.C_CHAR, maxStringLength)
      val retCode = HIGHSStatus.fromValue(HIGHS.Highs_getColName(highsPtr, colIndex, strPtr))
      if (retCode != HIGHSStatus.OK) {
        log.error { "Highs_getColName($colIndex) got return code $retCode" }
        return null
      }
      return strPtr.getString(0)
    }
  }

  fun getVarType(colIndex: Int): HIGHSVarType? {
    checkOpen()
    Arena.ofConfined().use {
      val colIntegrality = it.allocate(HIGHS.C_INT)
      val retCode = HIGHSStatus.fromValue(HIGHS.Highs_getColIntegrality(highsPtr, colIndex, colIntegrality))
      if (retCode != HIGHSStatus.OK) {
        log.error { "Highs_getColIntegrality($colIndex) got return code $retCode want OK " }
        return null
      }
      return HIGHSVarType.fromValue(colIntegrality.get(HIGHS.C_INT, 0))
    }
  }

  fun createConstraint(
    name: String,
    lb: Double,
    ub: Double,
    varAndCoefficients: List<Pair<Int, Double>>,
  ): Int? {
    checkOpen()
    Arena.ofConfined().use {
      var retCode =
        HIGHSStatus.fromValue(
          HIGHS.Highs_addRow(
            highsPtr,
            lb,
            ub,
            varAndCoefficients.size + 1,
            it.allocateFrom(HIGHS.C_INT, 0, *varAndCoefficients.map { v -> v.first }.toIntArray()),
            it.allocateFrom(
              HIGHS.C_DOUBLE,
              0.0,
              *varAndCoefficients
                .map { v ->
                  v.second
                }.toDoubleArray(),
            ),
          ),
        )
      if (retCode != HIGHSStatus.OK) {
        log.error { "Constraint $name: Highs_addRow(...) got status $retCode want OK" }
        return null
      }
      val constraintIndex = constraintCtr.getAndIncrement()
      retCode =
        HIGHSStatus.fromValue(
          HIGHS.Highs_passRowName(
            highsPtr,
            constraintIndex,
            it.allocateFrom(name),
          ),
        )
      if (retCode != HIGHSStatus.OK) {
        log.error { "Constraint $name: Highs_passRowName(...) got status $retCode want OK" }
        return null
      }
      return constraintIndex
    }
  }

  fun getConstraintByName(name: String): Int? {
    checkOpen()
    Arena.ofConfined().use {
      val rowIndex = it.allocate(HIGHS.C_INT)
      val retCode = HIGHSStatus.fromValue(HIGHS.Highs_getRowByName(highsPtr, it.allocateFrom(name), rowIndex))
      if (retCode != HIGHSStatus.OK) {
        log.error { "Highs_getRowByName($name) got return code $retCode" }
        return null
      }
      return rowIndex.get(HIGHS.C_INT, 0)
    }
  }

  fun getConstraintName(rowIndex: Int): String? {
    checkOpen()
    Arena.ofConfined().use {
      val strPtr = it.allocate(HIGHS.C_CHAR, maxStringLength)
      val retCode = HIGHSStatus.fromValue(HIGHS.Highs_getRowName(highsPtr, rowIndex, strPtr))
      if (retCode != HIGHSStatus.OK) {
        log.error { "Highs_getRowName($rowIndex) got return code $retCode" }
        return null
      }
      return strPtr.getString(0)
    }
  }

  fun getObjectiveDirection(): HIGHSObjective? {
    checkOpen()
    Arena.ofConfined().use {
      val intPtr = it.allocate(HIGHS.C_INT)
      val retCode = HIGHSStatus.fromValue(HIGHS.Highs_getObjectiveSense(highsPtr, intPtr))
      if (retCode != HIGHSStatus.OK) {
        log.error { "Highs_getObjectiveSense() want ok got $retCode" }
        return null
      }
      return HIGHSObjective.fromValue(intPtr.get(HIGHS.C_INT, 0))
    }
  }

  fun changeObjectiveDirection(obj: HIGHSObjective): HIGHSStatus {
    checkOpen()
    return HIGHSStatus.fromValue(HIGHS.Highs_changeObjectiveSense(highsPtr, obj.value))
  }

  fun getObjectiveOffset(): Double? {
    checkOpen()
    Arena.ofConfined().use {
      val dblPtr = it.allocate(HIGHS.C_DOUBLE)
      val retCode = HIGHSStatus.fromValue(HIGHS.Highs_getObjectiveOffset(highsPtr, dblPtr))
      if (retCode != HIGHSStatus.OK) {
        log.error { "Highs_getObjectiveOffset() want OK got $retCode" }
        return null
      }
      return dblPtr.get(HIGHS.C_DOUBLE, 0)
    }
  }

  fun changeObjectiveCoefficient(
    varIndex: Int,
    coeff: Double,
  ): HIGHSStatus {
    checkOpen()
    return HIGHSStatus.fromValue(HIGHS.Highs_changeColCost(highsPtr, varIndex, coeff))
  }

  fun changeObjectiveOffset(offset: Double): HIGHSStatus {
    checkOpen()
    return HIGHSStatus.fromValue(HIGHS.Highs_changeObjectiveOffset(highsPtr, offset))
  }

  fun run(): HIGHSStatus {
    checkOpen()
    return HIGHSStatus.fromValue(HIGHS.Highs_run(highsPtr))
  }

  fun getModelStatus(): HIGHSModelStatus {
    checkOpen()
    return HIGHSModelStatus.fromValue(HIGHS.Highs_getModelStatus(highsPtr))
  }

  fun infinity(): Double {
    checkOpen()
    return HIGHS.Highs_getInfinity(highsPtr)
  }

  fun negInfinity(): Double {
    checkOpen()
    return infinity() * -1
  }

  fun writeModel(filename: String): HIGHSStatus {
    checkOpen()
    return Arena.ofConfined().use {
      HIGHSStatus.fromValue(HIGHS.Highs_writeModel(highsPtr, it.allocateFrom(filename)))
    }
  }

  fun writeSolution(filename: String): HIGHSStatus {
    checkOpen()
    return Arena.ofConfined().use {
      HIGHSStatus.fromValue(HIGHS.Highs_writeSolutionPretty(highsPtr, it.allocateFrom(filename)))
    }
  }

  fun getSolution(): Result? {
    checkOpen()
    Arena.ofConfined().use {
      val col = it.allocate(HIGHS.C_DOUBLE, varCtr.get().toLong())
      val colDual = it.allocate(HIGHS.C_DOUBLE, varCtr.get().toLong())
      val row = it.allocate(HIGHS.C_DOUBLE, constraintCtr.get().toLong())
      val rowDual = it.allocate(HIGHS.C_DOUBLE, constraintCtr.get().toLong())
      val status = HIGHSStatus.fromValue(HIGHS.Highs_getSolution(highsPtr, col, colDual, row, rowDual))
      if (status != HIGHSStatus.OK) {
        log.error { "getSolution() want OK got $status" }
        return null
      }
      val colPrimalArray = col.toArray(HIGHS.C_DOUBLE)
      val colDualArray = colDual.toArray(HIGHS.C_DOUBLE)
      val rowPrimalArray = row.toArray(HIGHS.C_DOUBLE)
      val rowDualArray = rowDual.toArray(HIGHS.C_DOUBLE)
      if (colPrimalArray.size != colDualArray.size || colPrimalArray.size != varCtr.get()) {
        log.error {
          "Expected size of column bounds ${varCtr.get()} got primal ${colPrimalArray.size} dual ${colDualArray.size}"
        }
        return null
      }
      if (rowPrimalArray.size != rowDualArray.size || rowPrimalArray.size != constraintCtr.get()) {
        log.error {
          "Expected size of row bounds ${constraintCtr.get()} got primal ${rowPrimalArray.size} dual ${rowDualArray.size}"
        }
        return null
      }
      val cols = mutableMapOf<Int, Bounds>()
      val rows = mutableMapOf<Int, Bounds>()

      for (i in colPrimalArray.indices) {
        cols[i] = Bounds(primal = colPrimalArray[i], dual = colDualArray[i])
      }
      for (i in rowPrimalArray.indices) {
        rows[i] = Bounds(primal = rowPrimalArray[i], dual = rowDualArray[i])
      }
      return Result(cols = cols, rows = rows)
    }
  }

  fun getOutputInfo(param: HIGHSInfoParam): Number? {
    checkOpen()
    Arena.ofConfined().use {
      val infoTypeMemorySegment = it.allocate(HIGHS.C_INT)
      var status =
        HIGHSStatus.fromValue(HIGHS.Highs_getInfoType(highsPtr, it.allocateFrom(param.param), infoTypeMemorySegment))
      if (status != HIGHSStatus.OK) {
        log.error { "getInfoType($param) want OK got $status" }
        return null
      }
      val infoType = HIGHSInfoType.fromValue(infoTypeMemorySegment.get(HIGHS.C_INT, 0))
      when (infoType) {
        null -> {
          log.error { "infoType (${infoTypeMemorySegment.get(HIGHS.C_INT, 0)}) not supported in the implementation" }
          return null
        }

        HIGHSInfoType.INT -> {
          val infoVal = it.allocate(HIGHS.C_INT)
          status = HIGHSStatus.fromValue(HIGHS.Highs_getIntInfoValue(highsPtr, it.allocateFrom(param.param), infoVal))
          if (status != HIGHSStatus.OK) {
            log.error { "getIntInfoValue($param) want OK got $status" }
            return null
          }
          return infoVal.get(HIGHS.C_INT, 0)
        }

        HIGHSInfoType.INT64 -> {
          val infoVal = it.allocate(HIGHS.C_LONG)
          status = HIGHSStatus.fromValue(HIGHS.Highs_getInt64InfoValue(highsPtr, it.allocateFrom(param.param), infoVal))
          if (status != HIGHSStatus.OK) {
            log.error { "getInt64InfoValue($param) want OK got $status" }
            return null
          }
          return infoVal.get(HIGHS.C_LONG, 0)
        }

        HIGHSInfoType.DOUBLE -> {
          val infoVal = it.allocate(HIGHS.C_DOUBLE)
          status =
            HIGHSStatus.fromValue(HIGHS.Highs_getDoubleInfoValue(highsPtr, it.allocateFrom(param.param), infoVal))
          if (status != HIGHSStatus.OK) {
            log.error { "getDoubleInfoValue($param) want OK got $status" }
            return null
          }
          return infoVal.get(HIGHS.C_DOUBLE, 0)
        }
      }
    }
  }
}

data class Bounds(
  val primal: Double,
  val dual: Double,
)

data class Result(
  val cols: Map<Int, Bounds>,
  val rows: Map<Int, Bounds>,
)