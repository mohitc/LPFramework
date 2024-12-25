package com.lpapi.solver

import com.lpapi.model.LPModel
import com.lpapi.solver.enums.LPSolutionStatus

interface LPSolver<T> {
  /** Function to initialize the model in the solver based on the model specification in the LPModel
   * model
   */
  fun initialize(model: LPModel)

  /** Function to get the base model in order to enable configuration of model parameters if required
   */
  fun getBaseModel() : T?

  /** Function to start the computation of the model, and return the solution status.
   */
  fun solve() : LPSolutionStatus
}