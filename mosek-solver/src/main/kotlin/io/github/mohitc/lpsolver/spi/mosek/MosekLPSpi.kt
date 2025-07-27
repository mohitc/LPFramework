package io.github.mohitc.lpsolver.spi.mosek

import com.mosek.mosek.Task
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpsolver.LPSolver
import io.github.mohitc.lpsolver.mosek.MosekLPSolver
import io.github.mohitc.lpsolver.spi.LPSpi

class MosekLPSpi : LPSpi<Task> {
  override fun create(model: LPModel): LPSolver<Task> = MosekLPSolver(model)
}