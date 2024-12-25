package com.lpapi.model

import com.lpapi.model.enums.LPSolutionStatus
import com.lpapi.model.validators.LPConstraintValidator
import com.lpapi.model.validators.LPParamIdValidator
import com.lpapi.model.validators.LPParameterValidator
import com.lpapi.model.validators.LPVarValidator
import mu.KotlinLogging
import java.util.Locale

class LPModel(
  val identifier: String,
) {
  private val log = KotlinLogging.logger("LPModel")

  /** No name constructor, which sets the model name to Default
   */
  constructor() : this("Default")

  /**Constraints, variables and constraints can always be grouped, and are defined as LPParameterGroups
   */
  var constants = LPParameterGroup<LPConstant>(DEFAULT_CONSTANT_GROUP)

  var variables = LPParameterGroup<LPVar>(DEFAULT_VARIABLE_GROUP)

  var constraints = LPParameterGroup<LPConstraint>(DEFAULT_CONSTRAINT_GROUP)

  companion object {
    /** Names of the default groups used when not defined explicitly */
    const val DEFAULT_CONSTANT_GROUP = "Default"
    const val DEFAULT_VARIABLE_GROUP = "Default"
    const val DEFAULT_CONSTRAINT_GROUP = "Default"
  }

  private val constantValidator: List<LPParameterValidator<LPConstant>> = listOf(LPParamIdValidator())

  private val variableValidator: List<LPParameterValidator<LPVar>> = listOf(LPParamIdValidator(), LPVarValidator())

  private val constraintValidator: List<LPParameterValidator<LPConstraint>> =
    listOf(LPParamIdValidator(), LPConstraintValidator())

  /**Default to empty objective */
  val objective: LPObjective = LPObjective()

  /**The result of the model computation is populated in this variable*/
  var solution: LPModelResult? = null

  /** Function to reduce the objective function expression to the format where all variables have a single double
   * coefficient, and a single constant term. In case the value for any constant identifier is not found in the model,
   * a null value is returned
   */
  fun reduce(objective: LPObjective): LPObjective? {
    val reducedObjectiveExpression = reduce(objective.expression)
    // If a reduced expression is available for the objective, use that to generate the objective function
    return if (reducedObjectiveExpression != null) {
      LPObjective(objective.objective, reducedObjectiveExpression)
    } else {
      null
    }
  }

  /** Function to reduce the constraint to the format where all variable terms are on the LHS, with single instances of
   * a variable identifier, and the constant on the RHS. All fields with constant identifiers are replaced with the
   * actual constant values. E.g. aX + bY + c < mX + n => (a-m)X + bY < n-C
   * In case the value for any constant identifier is not found in the model, or if the expression does not have any
   * variables, a null value is returned
   */
  fun reduce(constraint: LPConstraint): LPConstraint? {
    val varMap: MutableMap<String, Double> = mutableMapOf()
    var constant = 0.0

    val reducedLhs = this.reduce(constraint.lhs) ?: return null
    val reducedRhs = this.reduce(constraint.rhs) ?: return null

    // Function to process each term for the reduction into a simpler expression. Terms with variables are incorporated
    // into the varMap, and constant terms are accumulated in the constant var
    fun processTerm(
      term: LPExpressionTerm,
      isLhs: Boolean,
    ) {
      val constMultiplier = if (isLhs) -1.0 else 1.0 // Constants are on the RHS, so value from LHS is subtracted
      val varMultiplier = constMultiplier * -1 // Variables have the opposite treatment to the constants
      val constantTerm = term.coefficient!!
      if (term.isConstant()) {
        constant += constMultiplier * constantTerm
      } else {
        // For each term in the LHS, create a map that includes the variable identifier and the computed double value
        varMap[term.lpVarIdentifier!!] = varMap.getOrPut(term.lpVarIdentifier, { 0.0 }) +
          varMultiplier * constantTerm
      }
    }

    reducedLhs.expression.forEach { processTerm(it, isLhs = true) }
    reducedRhs.expression.forEach { processTerm(it, isLhs = false) }

    if (varMap.isEmpty()) {
      log.error { "Constraint ${constraint.identifier} has term with no variables which is not a valid constraint" }
      return null
    }

    val newLPConstraint = LPConstraint(constraint.identifier)
    newLPConstraint.operator = constraint.operator

    varMap.entries.forEach { entry -> newLPConstraint.lhs.addTerm(entry.value, entry.key) }
    newLPConstraint.rhs.add(constant)
    return newLPConstraint
  }

  /** Function to reduce an expression to the format where all variables have a single double coefficient, and a single
   * constant term. In case the value for any constant identifier is not found in the model, a null value is returned
   */
  fun reduce(expression: LPExpression): LPExpression? {
    val varMap: MutableMap<String, Double> = mutableMapOf()
    var constant = 0.0
    var hasConstantTerm = false
    expression.expression.forEach { term ->
      if (!(term.lpConstantIdentifier == null || this.constants.exists(term.lpConstantIdentifier))) {
        log.error {
          "Expression has term with constant identifier ${term.lpConstantIdentifier} which is not defined " +
            "in the model"
        }
        return null
      } else if (term.lpConstantIdentifier == null && term.coefficient == null) {
        log.error { "term $term has both constant identifier and coefficient set to null" }
        return null
      }
      if (term.isConstant()) {
        hasConstantTerm = true
        // Sum up all constant terms
        constant += term.coefficient ?: constants.get(term.lpConstantIdentifier!!)?.value!!
      } else {
        // For each expression term, create a map that includes the variable identifier and the computed double value
        varMap[term.lpVarIdentifier!!] = (
          varMap.getOrPut(term.lpVarIdentifier, { 0.0 }) +
            (term.coefficient ?: constants.get(term.lpConstantIdentifier!!)?.value!!)
        )
      }
    }

    // check that all variables have been initialized
    val unknownVars = varMap.keys.filter { !this.variables.exists(it) }
    if (unknownVars.isNotEmpty()) {
      log.error { "Expression has terms with vars $unknownVars which have not been initialized" }
      return null
    }

    // Initialize new expression
    val reducedExpression = LPExpression()
    if (hasConstantTerm) {
      reducedExpression.add(constant)
    }
    varMap.entries.forEach { entry -> reducedExpression.addTerm(entry.value, entry.key) }
    return reducedExpression
  }

  fun validate(): Boolean {
    fun <T : LPParameter> parameterValidation(
      validatorList: List<LPParameterValidator<T>>,
      paramGroup: LPParameterGroup<T>,
      displayType: String,
    ): Boolean {
      log.debug { "Validating all ${displayType.lowercase(Locale.getDefault())} in the model" }
      val validationResult =
        validatorList
          .map { validator ->
            paramGroup
              .allValues()
              .stream()
              .map { v -> validator.validate(v, this) }
              .reduce { u, v -> u && v }
              .orElse(true)
          }.reduce { u, v -> u && v }
      log.debug { "$displayType validation result : $validationResult" }
      // idValidation being present means
      if (!validationResult) {
        log.error { "$displayType validation failed" }
      }
      return validationResult
    }

    return parameterValidation(constantValidator, constants, "Constants") &&
      // Constant validation
      parameterValidation(variableValidator, variables, "Variable") &&
      // Variable validation
      parameterValidation(constraintValidator, constraints, "Constraint") // Constraint validation
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LPModel

    if (identifier != other.identifier) return false
    if (constants != other.constants) return false
    if (variables != other.variables) return false
    if (constraints != other.constraints) return false
    if (objective != other.objective) return false
    if (solution != other.solution) return false

    return true
  }

  override fun hashCode(): Int {
    var result = identifier.hashCode()
    result = 31 * result + constants.hashCode()
    result = 31 * result + variables.hashCode()
    result = 31 * result + constraints.hashCode()
    result = 31 * result + objective.hashCode()
    result = 31 * result + (solution?.hashCode() ?: 0)
    return result
  }
}

/** Parameters in the LP model (variables, constraints, Constants), can all be modeled as groups of parameters.
 * A parameter may belong to the default group (identified in the constructor), if not specified, and otherwise
 * belongs to the group that is specified explicitly. The grouping is useful only for accessing the model parameters
 * easily after the creation of the model
 */
class LPParameterGroup<T : LPParameter>(
  private val defaultGroupIdentifier: String,
) {
  private val log = KotlinLogging.logger("LPParameterGroup")

  /**Grouping is the map which uses the group identifier as the  group keys, and provides the set of LPParameter
   * identifiers that are mapped against each grouping */
  var grouping: MutableMap<String, MutableSet<String>> = mutableMapOf()

  /**Parameters is the map which maps the  LPParameter identifiers against the LPParameter objects. */
  var parameters: MutableMap<String, T> = mutableMapOf()

  fun add(value: T): T? = add(defaultGroupIdentifier, value)

  /**Add a value to the parameter group, and return the value if everything is okay in the model, otherwise return
   * a null value
   */
  fun add(
    group: String,
    value: T,
  ): T? {
    if (parameters.containsKey(value.identifier)) {
      log.error { "${value.javaClass.simpleName} with identifier ${value.identifier} already exists." }
      return null
    }
    log.info { "Adding ${value.javaClass.simpleName} ${value.identifier} to group $group" }
    grouping.getOrPut(group, { mutableSetOf() }).add(value.identifier)
    parameters[value.identifier] = value
    return value
  }

  fun exists(identifier: String): Boolean = parameters.containsKey(identifier)

  fun get(identifier: String): T? = parameters[identifier]

  fun getAllGroups(): Set<String> = grouping.keys.toSet()

  fun getAllIdentifiers(group: String): Set<String>? = grouping[group]?.toSet()

  fun allValues(): MutableCollection<T> = parameters.values

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LPParameterGroup<*>

    if (grouping != other.grouping) return false
    if (parameters != other.parameters) return false

    return true
  }

  override fun hashCode(): Int {
    var result = grouping.hashCode()
    result = 31 * result + parameters.hashCode()
    return result
  }
}

/** Class to store the results from an LP computation */
class LPModelResult(
  val status: LPSolutionStatus,
  val objective: Double?,
  val computationTime: Long?,
  val mipGap: Double?,
) {
  constructor(status: LPSolutionStatus) : this(status, null, null, null)

  override fun toString(): String =
    "LPModelResult(status=$status, objective=$objective, computationTime=$computationTime, mipGap=$mipGap)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LPModelResult

    if (status != other.status) return false
    if (objective != other.objective) return false
    if (computationTime != other.computationTime) return false
    if (mipGap != other.mipGap) return false

    return true
  }

  override fun hashCode(): Int {
    var result = status.hashCode()
    result = 31 * result + (objective?.hashCode() ?: 0)
    result = 31 * result + (computationTime?.hashCode() ?: 0)
    result = 31 * result + (mipGap?.hashCode() ?: 0)
    return result
  }
}