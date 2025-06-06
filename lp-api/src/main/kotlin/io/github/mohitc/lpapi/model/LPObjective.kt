package io.github.mohitc.lpapi.model

import io.github.mohitc.lpapi.model.enums.LPObjectiveType

/** The LP Objective defines the objective function used in the model. The objective function is defined as a linear
 * expression that should be minimized or maximized (as defined by the objective type)
 */
class LPObjective(
  var objective: LPObjectiveType = LPObjectiveType.MAXIMIZE,
  var expression: io.github.mohitc.lpapi.model.LPExpression =
    io.github.mohitc.lpapi.model
      .LPExpression(),
) {
  override fun toString(): String = objective.shortDesc + "(" + expression.toString() + ")"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LPObjective

    if (objective != other.objective) return false
    if (expression != other.expression) return false

    return true
  }

  override fun hashCode(): Int {
    var result = objective.hashCode()
    result = 31 * result + expression.hashCode()
    return result
  }
}