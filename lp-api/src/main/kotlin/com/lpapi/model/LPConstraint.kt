package com.lpapi.model

import com.lpapi.model.enums.LPOperator

/** The LP Constraint defined a generic liner programming constraint which is defined via a linear expression on the
 * left hand side (lhs), a linear expression on the right hand side, and an operator. The format of the constraint
 * should be
 * lhs OPERATOR rhs
 */
class LPConstraint(
  override val identifier: String,
  val lhs: LPExpression,
  var operator: LPOperator,
  val rhs: LPExpression
) : LPParameter {

  /** Default constructor that generates a constraint with a default operation where the LHS is greater than the RHS
   */
  constructor(identifier: String) : this(identifier, LPExpression(), LPOperator.GREATER_EQUAL, LPExpression())

  override fun toString(): String = "$identifier: $lhs ${operator.shortDesc} $rhs"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LPConstraint

    if (identifier != other.identifier) return false
    if (lhs != other.lhs) return false
    if (operator != other.operator) return false
    if (rhs != other.rhs) return false

    return true
  }

  override fun hashCode(): Int {
    var result = identifier.hashCode()
    result = 31 * result + lhs.hashCode()
    result = 31 * result + operator.hashCode()
    result = 31 * result + rhs.hashCode()
    return result
  }
}