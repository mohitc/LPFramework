package com.lpapi.solver.sample.glpk

import com.lpapi.model.LPModel
import com.lpapi.solver.enums.LPSolutionStatus
import com.lpapi.solver.glpk.GlpkLpSolver
import com.lpapi.solver.sample.PrimitiveSolverSample

class GplkPrimitiveSolverSample : PrimitiveSolverSample() {

  override fun initAndSolveModel(model: LPModel): LPModel? {
    if (!model.validate()) {
      log.error { "Model could not be validated " }
      return null
    }
    val solver = GlpkLpSolver(model)
    solver.initialize()
    val status = solver.solve()
    return if (status!=LPSolutionStatus.UNKNOWN &&
        status!=LPSolutionStatus.INFEASIBLE && status!=LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED)
      solver.model
    else
      null
  }

}

fun main(args: Array<String>) {
  val solver = GplkPrimitiveSolverSample()
  solver.log.info { "Starting test. Checking if model is initialized and solved correctly" }
  val solvedModel = solver.initAndSolveModel(solver.initLpModel())
  if (solvedModel==null)
    solver.log.error { "Solver should have solved model correctly" }
}

