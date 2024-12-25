package com.lpapi.solver

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPSolutionStatus
import mu.KotlinLogging

abstract class LPSolver<T>(val model: LPModel) {

  val log = KotlinLogging.logger(this.javaClass.simpleName)

  /** Function to initialize the model in the solver based on the model specification in the LPModel
   * model
   */
  fun initialize(): Boolean {
    try {
      if (!initModel() || !initVars() || !initConstraints())
        return false
      return initObjectiveFunction()
    } catch (e: Exception) {
      log.error { "Unexpected error while initializing model $e" }
      return false
    }
  }

  /** Function to initialize the model in the solver based on the model specification in the LPModel
   * model
   */
  abstract fun initModel(): Boolean

  /** Function to get the base model in order to enable configuration of model parameters if required
   */
  abstract fun getBaseModel(): T?

  /** Function to start the computation of the model, and return the solution status.
   */
  abstract fun solve(): LPSolutionStatus

  /**Function to initialize the variables in the model
   */
  abstract fun initVars(): Boolean

  /**Function to initialize the constraints in the model
   */
  abstract fun initConstraints(): Boolean

  /**Function to initialize the variables in the model
   */
  abstract fun initObjectiveFunction(): Boolean
}