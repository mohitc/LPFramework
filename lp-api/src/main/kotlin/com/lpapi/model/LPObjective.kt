package com.lpapi.model

import com.lpapi.model.enums.LPObjectiveType

/** The LP Objective defines the objective function used in the model. The objective function is defined as a linear
 * expression that should be minimized or maximized (as defined by the objective type)
 */
class LPObjective constructor(var objective: LPObjectiveType, var expression: LPExpression, var result: Double?) {

  constructor(objective: LPObjectiveType) : this(objective, LPExpression(), null)

  constructor() : this(LPObjectiveType.MAXIMIZE, LPExpression(), null)

  override fun toString(): String = objective.shortDesc + "(" + expression.toString() + ")"
}