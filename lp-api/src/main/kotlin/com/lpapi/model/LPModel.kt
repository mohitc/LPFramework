package com.lpapi.model

import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.validators.LPConstraintValidator
import com.lpapi.model.validators.LPParamIdValidator
import com.lpapi.model.validators.LPParameterValidator
import com.lpapi.model.validators.LPVarValidator
import mu.KotlinLogging

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

  /**Default to empty objective */
  val objective : LPObjective = LPObjective()

  /**The result of the model computation is populated in this variable*/
  var solution: LPModelResult? = null

  /** Function to reduce the objective function expression to the format where all variables have a single double coefficient,
   * and a single constant term. In case the value for any constant identifier is not found in the model, a null value is returned
   */
  fun reduce(objective: LPObjective) : LPObjective? {
    val reducedObjectiveExpression = reduce(objective.expression)
    //If a reduced expression is available for the objective, use that to generate the objective function
    return if (reducedObjectiveExpression !=null)
      LPObjective(objective.objective, reducedObjectiveExpression)
    else
      null
  }

  /** Function to reduce the constraint to the format where all variable terms are on the LHS, with single instances of a
   * variable identifier, and the constant on the RHS. All fields with constant identifiers are replaced with the actual
   * constant values. E.g. aX + bY + c < mX + n => (a-m)X + bY < n-C
   * In case the value for any constant identifier is not found in the model, or if the expression does not have any
   * variables, a null value is returned
   */
  fun reduce(constraint: LPConstraint) : LPConstraint? {
    val varMap : MutableMap<String, Double> = mutableMapOf()
    var constant = 0.0
    constraint.lhs.expression.forEach{ term ->
      if (!(term.lpConstantIdentifier == null || this.constants.exists(term.lpConstantIdentifier))) {
        log.error { "Constraint ${constraint.identifier} has term with constant identifier ${term.lpConstantIdentifier} which is not defined in the model" }
        return null
      }
      if (term.isConstant()) {
        //LHS constant terms are moved to the RHS
        constant -= term.coefficient ?: constants.get(term.lpConstantIdentifier!!)?.value!!
      } else
        // For each term in the LHS, create a map that includes the variable identifier and the double value computed till now
        varMap[term.lpVarIdentifier!!] = (varMap.getOrPut(term.lpVarIdentifier, { 0.0 })
            + (term.coefficient ?: constants.get(term.lpConstantIdentifier!!)?.value!!))
    }

    constraint.rhs.expression.forEach{ term ->
      if (!(term.lpConstantIdentifier == null || this.constants.exists(term.lpConstantIdentifier))) {
        log.error { "Constraint ${constraint.identifier} has term with constant identifier ${term.lpConstantIdentifier} which is not defined in the model" }
        return null
      }
      if (term.isConstant()) {
        //RHS constant terms are kept on the right hand side
        constant += term.coefficient ?: constants.get(term.lpConstantIdentifier!!)?.value!!
      } else
      // Terms in the RHS are moved to the LHS
        varMap[term.lpVarIdentifier!!] = (varMap.getOrPut(term.lpVarIdentifier, { 0.0 })
            - (term.coefficient ?: constants.get(term.lpConstantIdentifier!!)?.value!!))
    }

    if (varMap.size==0) {
      log.error { "Constraint ${constraint.identifier} has term with no variables which is not a valid constraint" }
      return null
    }
    val newLPConstraint = LPConstraint(constraint.identifier)
    newLPConstraint.operator = constraint.operator

    varMap.entries.forEach { entry -> newLPConstraint.lhs.addTerm(entry.value, entry.key) }
    newLPConstraint.rhs.add(constant)
    return newLPConstraint
  }

  /** Function to reduce an expression to the format where all variables have a single double coefficient,
   * and a single constant term. In case the value for any constant identifier is not found in the model, a null value is returned
   */
  fun reduce(expression: LPExpression) : LPExpression? {
    val varMap : MutableMap<String, Double> = mutableMapOf()
    var constant = 0.0
    var hasConstantTerm = false
    expression.expression.forEach{ term ->
      if (!(term.lpConstantIdentifier == null || this.constants.exists(term.lpConstantIdentifier))) {
        log.error { "Expression has term with constant identifier ${term.lpConstantIdentifier} which is not defined in the model" }
        return null
      }
      if (term.isConstant()) {
        hasConstantTerm = true
        //Sum up all constant terms
        constant += term.coefficient ?: constants.get(term.lpConstantIdentifier!!)?.value!!
      } else
      // For each term in the expression, create a map that includes the variable identifier and the double value computed till now
        varMap[term.lpVarIdentifier!!] = (varMap.getOrPut(term.lpVarIdentifier, { 0.0 })
            + (term.coefficient ?: constants.get(term.lpConstantIdentifier!!)?.value!!))
    }

    //Initialize new objective function
    val reducedExpression = LPExpression()
    if (hasConstantTerm)
      reducedExpression.add(constant)
    varMap.entries.forEach { entry -> reducedExpression.addTerm(entry.value, entry.key) }
    return reducedExpression
  }


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

  fun getAllGroups() : Set<String> = grouping.keys.toSet()

  fun getAllIdentifiers(group: String) : Set<String>? {
    return grouping[group]?.toSet()
  }

  fun allValues() : MutableCollection<T> {
    return parameters.values
  }

}

/** Class to store the results from an LP computation */
class LPModelResult (val status: LPSolutionStatus, val objective: Double?, val computationTime: Long?, val mipGap: Double?) {

  constructor(solnStatus: LPSolutionStatus) : this(solnStatus, null, null, null)

  override fun toString(): String {
    return "LPModelResult(status=$status, objective=$objective, computationTime=$computationTime, mipGap=$mipGap)"
  }
}