package com.lpapi.model.validators

import com.lpapi.model.*
import com.lpapi.model.enums.LPVarType
import mu.KotlinLogging
import kotlin.math.roundToInt

interface LPParameterValidator<T : LPParameter> {
  fun validate(instance: T, model: LPModel) : Boolean
}

/** The LPParameterIdValidator checks if the entity is of the supported types, namely a constant, constraint or a variable
 * Additionally, the mechanism checks if the model parameterGroupings have the references to the entity provided correctly
 */
class LPParamIdValidator<T: LPParameter> : LPParameterValidator<T> {
  private val log = KotlinLogging.logger ("LPValidator")

  /** Initializing instances of LPVariable, LPConstant and LPConstraint to check incoming parameter for validation
   * and extract the correct parameter grouping from the model if required
   */
  private val constantInstance =  LPConstant("c")
  private val varInstance = LPVar("v", LPVarType.BOOLEAN)
  private val constraintInstance = LPConstraint("cn")

  override fun validate(instance: T, model: LPModel): Boolean {
    //Identify the grouping in the model, and if found make sure that both the identifier map and the grouping map have
    //a reference to the instance identifier. 
    val paramType = instance.javaClass.simpleName
    val parameterGrouping =
      when {
        instance.javaClass.isAssignableFrom(constantInstance.javaClass) -> model.constants
        instance.javaClass.isAssignableFrom(varInstance.javaClass) -> model.variables
        instance.javaClass.isAssignableFrom(constraintInstance.javaClass) -> model.constraints
        else -> null
      }

    if (parameterGrouping==null) {
      log.error { "$paramType is not supported in the Parameter ID Validation check (ID: ${instance.identifier})" }
      return false
    }

    val exists = parameterGrouping.exists(instance.identifier)
    if (!exists) {
      log.error { "$paramType with ID ${instance.identifier} not found in model." }
      return false
    }

    val noGroupingWithIdentifier = parameterGrouping.grouping.values.stream()
      .noneMatch { v -> v.contains(instance.identifier) }
    if (noGroupingWithIdentifier) {
      log.error { "No grouping for $paramType contains parameter with ID ${instance.identifier}" }
      return false
    }
    return true
  }
}

/** Validator to ensure that variables have the correct bounds defined
 */
class LPVarValidator : LPParameterValidator<LPVar> {
  private val log = KotlinLogging.logger("LPValidator")

  override fun validate(instance: LPVar, model: LPModel): Boolean {
    if (instance.lbound > instance.ubound) {
      log.error { "Variable ${instance.identifier} has a lower bound ${instance.lbound} greater than the upper bound ${instance.ubound}" }
      return false
    }
    if (instance.type == LPVarType.BOOLEAN) {
      if (instance.lbound > 1) {
        log.error { "Boolean Variable ${instance.identifier} has a lower bound ${instance.lbound} greater than 1" }
        return false
      } else if (instance.ubound < 0) {
        log.error { "Boolean Variable ${instance.identifier} has an upper bound ${instance.ubound} less than 0" }
        return false
      }
    }
    if (instance.type != LPVarType.DOUBLE) {
      //integer variable, should have atleast one valid result
      if ((instance.ubound + 0.5).roundToInt() - (instance.lbound + 0.5).roundToInt() ==0) {
        log.error { "${if (instance.type == LPVarType.INTEGER) "Integer" else "Boolean"} Variable ${instance.identifier} has no integer value in the covered bounds (${instance.lbound}, ${instance.ubound}" }
        return false
      }
    }
    return true
  }
}

/** Class to validate constraints provided in the model
 */
class LPConstraintValidator : LPParameterValidator<LPConstraint> {

  private val log = KotlinLogging.logger("LPValidator")

  override fun validate(instance: LPConstraint, model: LPModel): Boolean {
    //in a constraint, both expressions should contain atleast one terms
    if (instance.lhs.expression.size ==0 || instance.rhs.expression.size ==0) {
      log.error { "Constraint ${instance.identifier} has no terms defined in the ${if (instance.lhs.expression.size==0) "LHS" else "RHS"}" }
      return false
    }

    //Validate all terms in the expression of a constraint
    return listOf(instance.lhs, instance.rhs).flatMap { v -> v.expression }
      .map{
          if (it.coefficient == null && it.lpConstantIdentifier == null) {
            log.error { "Constraint ${instance.identifier} has term that has no constant reference or fixed value defined" }
            false
          } else if (it.coefficient != null && it.lpConstantIdentifier != null) {
            log.error { "Constraint ${instance.identifier} has term that has both constant reference ${it.lpConstantIdentifier} and fixed value coefficient ${it.coefficient}" }
            false
          } else if (it.lpConstantIdentifier != null && !model.constants.exists(it.lpConstantIdentifier)) {
            log.error { "Constraint ${instance.identifier} has term with constant reference ${it.lpConstantIdentifier} which is not defined in the model" }
            false
          } else if (it.lpVarIdentifier != null && !model.variables.exists(it.lpVarIdentifier)) {
            log.error { "Constraint ${instance.identifier} has term with variable reference ${it.lpVarIdentifier} which is not defined in the model" }
            false
          } else
            true
        }.reduce{u, v -> u && v}
  }

}