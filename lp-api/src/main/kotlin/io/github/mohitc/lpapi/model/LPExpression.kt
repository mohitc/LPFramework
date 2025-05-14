package io.github.mohitc.lpapi.model

/** The LPExpression is a linear expression which is described as a sum of multiple terms described in the
 * LPExpressionTerm.
 */
class LPExpression {
  var expression: MutableList<io.github.mohitc.lpapi.model.LPExpressionTerm> = mutableListOf()

  /** Add a constant value specified as a fixed Double value
   */
  fun add(value: Number): io.github.mohitc.lpapi.model.LPExpression {
    expression.add(
      io.github.mohitc.lpapi.model
        .LPExpressionTerm(value.toDouble(), null, null),
    )
    return this
  }

  /** Add a constant value specified as an LP Constant
   */
  fun add(constant: io.github.mohitc.lpapi.model.LPConstant): io.github.mohitc.lpapi.model.LPExpression {
    expression.add(
      io.github.mohitc.lpapi.model
        .LPExpressionTerm(null, null, constant.identifier),
    )
    return this
  }

  /** Add a constant value specified as an LP Constant identifier
   */
  fun add(constantIdentifier: String): io.github.mohitc.lpapi.model.LPExpression {
    expression.add(
      io.github.mohitc.lpapi.model
        .LPExpressionTerm(null, null, constantIdentifier),
    )
    return this
  }

  /** Add a variable term specified as an LP Variable (e.g. 1.X)
   */
  fun addTerm(variable: io.github.mohitc.lpapi.model.LPVar): io.github.mohitc.lpapi.model.LPExpression {
    expression.add(
      io.github.mohitc.lpapi.model
        .LPExpressionTerm(1.0, variable.identifier, null),
    )
    return this
  }

  /** Add a variable term specified as an LP Variable Identifier (e.g. 1.X)
   */
  fun addTerm(variableIdentifier: String): io.github.mohitc.lpapi.model.LPExpression {
    expression.add(
      io.github.mohitc.lpapi.model
        .LPExpressionTerm(1.0, variableIdentifier, null),
    )
    return this
  }

  /** Add a variable term specified as an LP Constant and Variable (e.g. c.X)
   */
  fun addTerm(
    constant: io.github.mohitc.lpapi.model.LPConstant,
    variable: io.github.mohitc.lpapi.model.LPVar,
  ): io.github.mohitc.lpapi.model.LPExpression {
    expression.add(
      io.github.mohitc.lpapi.model
        .LPExpressionTerm(null, variable.identifier, constant.identifier),
    )
    return this
  }

  /** Add a variable term specified as an LP Constant identifier and Variable identifier (e.g. c.X)
   */
  fun addTerm(
    constantIdentifier: String,
    variableIdentifier: String,
  ): io.github.mohitc.lpapi.model.LPExpression {
    expression.add(
      io.github.mohitc.lpapi.model
        .LPExpressionTerm(null, variableIdentifier, constantIdentifier),
    )
    return this
  }

  /** Add a variable term specified as a double and LP Variable (e.g. 3.X)
   */
  fun addTerm(
    constant: Number,
    variable: io.github.mohitc.lpapi.model.LPVar,
  ): io.github.mohitc.lpapi.model.LPExpression {
    expression.add(
      io.github.mohitc.lpapi.model
        .LPExpressionTerm(constant.toDouble(), variable.identifier, null),
    )
    return this
  }

  /** Add a variable term specified as a double  and Variable identifier (e.g. 3.X)
   */
  fun addTerm(
    constant: Number,
    variableIdentifier: String,
  ): io.github.mohitc.lpapi.model.LPExpression {
    expression.add(
      io.github.mohitc.lpapi.model
        .LPExpressionTerm(constant.toDouble(), variableIdentifier, null),
    )
    return this
  }

  /** Add all terms defined in an expression into another expression
   */
  fun add(expression: io.github.mohitc.lpapi.model.LPExpression): io.github.mohitc.lpapi.model.LPExpression {
    expression.expression.forEach { v -> this.expression.add(v) }
    return this
  }

  fun copy(): io.github.mohitc.lpapi.model.LPExpression {
    val newExpression =
      io.github.mohitc.lpapi.model
        .LPExpression()
    expression.forEach {
      newExpression.expression.add(
        io.github.mohitc.lpapi.model.LPExpressionTerm(
          it.coefficient,
          it.lpVarIdentifier,
          it.lpConstantIdentifier,
        ),
      )
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

    other as io.github.mohitc.lpapi.model.LPExpression

    return expression.toSet() == other.expression.toSet()
  }

  override fun hashCode(): Int = expression.toSet().hashCode()
}