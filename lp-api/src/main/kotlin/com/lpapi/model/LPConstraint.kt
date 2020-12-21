package com.lpapi.model

import com.lpapi.model.enums.LPOperator

/** The LP Constraint defined a generic liner programming constraint which is defined via a linear expression on the
 * left hand side (lhs), a linear expression on the right hand side, and an operator. The format of the constraint
 * should be
 * lhs OPERATOR rhs
 */
class LPConstraint constructor(
  override val identifier: String,
  val lhs: LPExpression,
  var operator: LPOperator,
  val rhs: LPExpression
) : LPParameter {

  /** Default constructor that generates a constraint with a default operation where the LHS is greater than the RHS
   */
  constructor(identifier: String) : this(identifier, LPExpression(), LPOperator.GREATER_EQUAL, LPExpression())

  override fun toString(): String = "$identifier: $lhs ${operator.shortDesc} $rhs"
}