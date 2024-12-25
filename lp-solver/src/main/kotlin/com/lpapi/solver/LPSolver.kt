package com.lpapi.solver

import com.lpapi.model.LPModel
import com.lpapi.solver.enums.LPSolutionStatus

abstract class LPSolver<T>(val model: LPModel) {

  /** Function to initialize the model in the solver based on the model specification in the LPModel
   * model
   */
  abstract fun initialize() : Boolean

  /** Function to get the base model in order to enable configuration of model parameters if required
   */
  abstract fun getBaseModel() : T?

  /** Function to start the computation of the model, and return the solution status.
   */
  abstract fun solve() : LPSolutionStatus
}