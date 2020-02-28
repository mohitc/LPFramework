package com.lpapi.model

import com.lpapi.model.validators.LPConstraintValidator
import com.lpapi.model.validators.LPParamIdValidator
import com.lpapi.model.validators.LPParameterValidator
import com.lpapi.model.validators.LPVarValidator
import mu.KotlinLogging
import java.util.stream.Collectors

class LPModel (val identifier: String){
  private val log = KotlinLogging.logger("LPModel")

  /** No name constructor, which sets the model name to Default
   */
  constructor() : this("Default")

  /**Constraints, variables and constraints can always be grouped, and are defined as LPParameterGroups
   */
  val DEFAULT_CONSTANT_GROUP = "Default"
  var constants = LPParameterGroup<LPConstant>(DEFAULT_CONSTANT_GROUP)

  val DEFAULT_VARIABLE_GROUP = "Default"
  var variables = LPParameterGroup<LPVar>(DEFAULT_VARIABLE_GROUP)

  val DEFAULT_CONSTRAINT_GROUP = "Default"
  var constraints = LPParameterGroup<LPConstraint>(DEFAULT_CONSTRAINT_GROUP)

  private val constantValidator : List<LPParameterValidator<LPConstant>> = listOf(LPParamIdValidator())

  private val variableValidator : List<LPParameterValidator<LPVar>> = listOf(LPParamIdValidator(), LPVarValidator())

  private val constraintValidator : List<LPParameterValidator<LPConstraint>> = listOf(LPParamIdValidator(), LPConstraintValidator())

  fun validate() : Boolean {

    log.debug { "Validating all constants in the model" }
    val constantsValidation = constantValidator.map { validator ->
      constants.allValues().stream()
          .map { v -> validator.validate(v, this) }
          .reduce{ u, v -> u && v }
          .orElse(true)
    }.reduce { u, v -> u && v }
    //idValidation being present means
    if (!constantsValidation) {
      log.error { "Constants validation failed" }
      return false
    }

    log.debug { "Validating all variables in the model" }
    val variableValidation = variableValidator.map { validator ->
      variables.allValues().stream()
          .map { v -> validator.validate(v, this) }
          .reduce{ u, v -> u && v }
          .orElse(true)
    }.reduce { u, v -> u && v }
    //idValidation being present means
    if (!variableValidation) {
      log.error { "Variable validation failed" }
      return false
    }

    log.debug { "Validating all constraints in the model" }
    val constraintValidation = constraintValidator.map { validator ->
      constraints.allValues().stream()
        .map { v -> validator.validate(v, this) }
        .reduce{ u, v -> u && v }
        .orElse(true)
    }.reduce { u, v -> u && v }
    //idValidation being present means
    if (!constraintValidation) {
      log.error { "Constraint validation failed" }
      return false
    }

    return true
  }
}

/** Parameters in the LP model (variables, constraints, Constants, can all be modeled as groups of parameters.
 * A parameter may belong to the default group (identified in the constructor), if not specified, and otherwise
 * belongs to the group that is specified explicitly. The grouping is useful only for accessing the model parameters
 * easily after the creation of the model
 */
class LPParameterGroup<T : LPParameter> (private val defaultGroupIdentifier: String) {
  private val log = KotlinLogging.logger("LPParameterGroup")

  /**Grouping is the map which uses the group identifier as the  group keys, and provides the set of LPParameter identifiers
   * that are mapped against each grouping */
  var grouping: MutableMap<String, MutableSet<String>> = mutableMapOf()
  /**Parameters is the map which maps the  LPParameter identifiers against the LPParameter objects. */
  var parameters: MutableMap<String, T> = mutableMapOf()

  fun add(value: T) : T? {
    return add(defaultGroupIdentifier, value)
  }

  /**Add a value to the parameter group, and return the value if everything is okay in the model, otherwise return
   * a null value
   */
  fun add(group: String, value: T) : T? {
    if (parameters.containsKey(value.identifier)) {
      log.error { "${value.javaClass.simpleName} with identifier ${value.identifier} already exists." }
      return null
    }
    log.info { "Adding ${value.javaClass.simpleName} ${value.identifier} to group $group"  }
    grouping.getOrPut(group, { mutableSetOf() }).add(value.identifier)
    parameters[value.identifier] = value
    return value
  }

  fun exists(identifier: String) : Boolean = parameters.containsKey(identifier)

  fun get (identifier: String) : T? = parameters[identifier]

  fun getAllGroups() : Set<String> = grouping.keys.stream().collect(Collectors.toUnmodifiableSet())

  fun getAllIdentifiers(group: String) : Set<String>? {
    return grouping[group]?.stream()?.collect(Collectors.toUnmodifiableSet())
  }

  fun allValues() : MutableCollection<T> {
    return parameters.values
  }

}