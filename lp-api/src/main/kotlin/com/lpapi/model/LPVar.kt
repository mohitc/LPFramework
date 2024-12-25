package com.lpapi.model

import com.lpapi.model.enums.LPVarType
import kotlin.math.roundToInt

class LPVar(
  override val identifier: String,
  val type: LPVarType,
  var lbound: Double,
  var ubound: Double
) : LPParameter {

  var result: Number = 0

  var resultSet: Boolean = false

  constructor(identifier: String, type: LPVarType, lbound: Number, ubound: Number) :
    this(identifier, type, lbound.toDouble(), ubound.toDouble())

  constructor(identifier: String, type: LPVarType) : this(
    identifier, type,
    // If variable is defined as boolean then upper bound is automatically set to 1 otherwise is defaulted to 0
    0.0, if (type == LPVarType.BOOLEAN) 1.0 else 0.0
  )

  fun bounds(lBound: Double, uBound: Double): LPVar {
    this.lbound = lBound
    this.ubound = uBound
    return this
  }

  fun populateResult(result: Number) {
    this.resultSet = true
    if (type === LPVarType.DOUBLE) {
      this.result = result
    } else {
      // value is an integer
      this.result = result.toDouble().roundToInt()
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LPVar

    if (identifier != other.identifier) return false
    if (type != other.type) return false
    if (lbound != other.lbound) return false
    if (ubound != other.ubound) return false
    if (result != other.result) return false
    if (resultSet != other.resultSet) return false

    return true
  }

  override fun hashCode(): Int {
    var result1 = identifier.hashCode()
    result1 = 31 * result1 + type.hashCode()
    result1 = 31 * result1 + lbound.hashCode()
    result1 = 31 * result1 + ubound.hashCode()
    result1 = 31 * result1 + result.hashCode()
    result1 = 31 * result1 + resultSet.hashCode()
    return result1
  }

  override fun toString(): String {
    return "[identifier: $identifier, type: $type]"
  }
}