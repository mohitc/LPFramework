package io.github.mohitc.lpsolver.test

interface SolverTestInstance {
  fun name(): String

  fun initModel(): Boolean

  fun solveAndValidate()
}