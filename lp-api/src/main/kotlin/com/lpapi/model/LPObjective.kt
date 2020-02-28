package com.lpapi.model

import com.lpapi.model.enums.LPObjectiveType

/** The LP Objective defines the objective function used in the model. The objective function is defined as a linear
 * expression that should be minimized or maximized (as defined by the objective type)
 */
class LPObjective constructor(var objective: LPObjectiveType, var expression: LPExpression) {

  constructor(objective: LPObjectiveType) : this(objective, LPExpression())

  override fun toString(): String = objective.shortDesc + "(" + expression.toString() + ")"
}