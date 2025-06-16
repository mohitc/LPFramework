# Example Instances

This package contains some examples for modeling MILP problems, and are also
used for integration tests for the various solvers supported in this framework.

Currently supported instances include:

* [
  `PrimitiveSolverSample`](src/main/kotlin/io/github/mohitc/lpsolver/sample/PrimitiveSolverSample.kt)
  is a very simple model useful for demonstrating the mechanism to build and
  solver models.
* [
  `SudokuSolverSample`](src/main/kotlin/io/github/mohitc/lpsolver/sample/SudokuSolverSample.kt)
  implements and solves a 9x9 Sudoku using ILP modeling, and is an example of a
  feasibility problem.
* [
  `KnapsackSolverSample`](src/main/kotlin/io/github/mohitc/lpsolver/sample/KnapsackSolverSample.kt)
  implements and solves the classic Knapsack problem.
* [
  `TravellingSalesmanSolverSample`](src/main/kotlin/io/github/mohitc/lpsolver/sample/TravellingSalesmanSolverSample.kt)
  generates a random instance of the Travelling Salesman problem, solves it
  using dynamic programming and via ILP formulation and validates that the
  solutions are equivalent.

In order to have the problems supported in integration tests, all problems
implement the [
`SolverTestInstance`](src/main/kotlin/io/github/mohitc/lpsolver/test/SolverTestInstance.kt)
Interface and are included in the [
`SolverTest`](src/main/kotlin/io/github/mohitc/lpsolver/test/SolverTest.kt)
class to be included in all existing solver tests. Samples of integration tests
for the various solvers can be found in the `ITSolverTest` classes under the
various solver implementations.