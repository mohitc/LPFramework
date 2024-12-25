package com.lpapi.model

/** The LPExpression is a linear expression which is described as a sum of multiple terms described in the
 * LPExpressionTerm.
 */
class LPExpression {
  var expression: MutableList<LPExpressionTerm> = mutableListOf()

  /** Add a constant value specified as a fixed Double value
   */
  fun add(value: Number): LPExpression {
    expression.add(LPExpressionTerm(value.toDouble(), null, null))
    return this
  }

  /** Add a constant value specified as an LP Constant
   */
  fun add(constant: LPConstant): LPExpression {
    expression.add(LPExpressionTerm(null, null, constant.identifier))
    return this
  }

  /** Add a constant value specified as an LP Constant identifier
   */
  fun add(constantIdentifier: String): LPExpression {
    expression.add(LPExpressionTerm(null, null, constantIdentifier))
    return this
  }

  /** Add a variable term specified as an LP Variable (e.g. 1.X)
   */
  fun addTerm(variable: LPVar): LPExpression {
    expression.add(LPExpressionTerm(1.0, variable.identifier, null))
    return this
  }

  /** Add a variable term specified as an LP Variable Identifier (e.g. 1.X)
   */
  fun addTerm(variableIdentifier: String): LPExpression {
    expression.add(LPExpressionTerm(1.0, variableIdentifier, null))
    return this
  }

  /** Add a variable term specified as an LP Constant and Variable (e.g. c.X)
   */
  fun addTerm(
    constant: LPConstant,
    variable: LPVar,
  ): LPExpression {
    expression.add(LPExpressionTerm(null, variable.identifier, constant.identifier))
    return this
  }

  /** Add a variable term specified as an LP Constant identifier and Variable identifier (e.g. c.X)
   */
  fun addTerm(
    constantIdentifier: String,
    variableIdentifier: String,
  ): LPExpression {
    expression.add(LPExpressionTerm(null, variableIdentifier, constantIdentifier))
    return this
  }

  /** Add a variable term specified as a double and LP Variable (e.g. 3.X)
   */
  fun addTerm(
    constant: Number,
    variable: LPVar,
  ): LPExpression {
    expression.add(LPExpressionTerm(constant.toDouble(), variable.identifier, null))
    return this
  }

  /** Add a variable term specified as a double  and Variable identifier (e.g. 3.X)
   */
  fun addTerm(
    constant: Number,
    variableIdentifier: String,
  ): LPExpression {
    expression.add(LPExpressionTerm(constant.toDouble(), variableIdentifier, null))
    return this
  }

  /** Add all terms defined in an expression into another expression
   */
  fun add(expression: LPExpression): LPExpression {
    expression.expression.forEach { v -> this.expression.add(v) }
    return this
  }

  fun copy(): LPExpression {
    val newExpression = LPExpression()
    expression.forEach {
      newExpression.expression.add(LPExpressionTerm(it.coefficient, it.lpVarIdentifier, it.lpConstantIdentifier))
    }
    return newExpression
  }

  override fun toString(): String =
    expression
      .stream()
      .map { v -> v.toString() }
      .reduce { acc, expTerm -> "$acc + $expTerm" }
      .orElse("")

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LPExpression

    return expression.toSet() == other.expression.toSet()
  }

  override fun hashCode(): Int = expression.toSet().hashCode()
}