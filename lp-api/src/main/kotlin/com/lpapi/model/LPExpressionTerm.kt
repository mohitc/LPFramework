package com.lpapi.model

/** The LPExpressionTerm defined an individual term in a liner expression. Each term can either be a constant,
 * (defined either by the identifier of the constant defined in the model, or a fixed value) or a dot product of
 * a constant value and a variable as defined in the model
 */
class LPExpressionTerm constructor(val coefficient: Double?, val lpVarIdentifier: String?, val lpConstantIdentifier: String?) {

  fun isConstant(): Boolean = lpVarIdentifier==null

  override fun toString() = "(" + (lpConstantIdentifier ?: coefficient.toString()) + (lpVarIdentifier ?: "") + ")"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LPExpressionTerm

    if (coefficient != other.coefficient) return false
    if (lpVarIdentifier != other.lpVarIdentifier) return false
    if (lpConstantIdentifier != other.lpConstantIdentifier) return false

    return true
  }

  override fun hashCode(): Int {
    var result = coefficient?.hashCode() ?: 0
    result = 31 * result + (lpVarIdentifier?.hashCode() ?: 0)
    result = 31 * result + (lpConstantIdentifier?.hashCode() ?: 0)
    return result
  }

}