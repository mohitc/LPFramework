package io.github.mohitc.lpsolver.sample

import io.github.mohitc.lpapi.model.LPConstant
import io.github.mohitc.lpapi.model.LPConstraint
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPVar
import io.github.mohitc.lpapi.model.enums.LPObjectiveType
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import io.github.mohitc.lpsolver.spi.Solver
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

open class TravellingSalesmanSolverSample {
  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  // This test implements the classical travelling salesman problem.
  // Ref: (https://en.wikipedia.org/wiki/Travelling_salesman_problem)
  // We use the MTZ formulation which defines an order on the vertices
  // also implement a brute force solver to compare solutions.

  // Objective: Min Sum (c_ij * X_ij) -- minimize distance travelled

  // Constraints to model the problem:
  // Sum_i (X_ij) = 1 -- exactly one exit from each node
  // Sum_j (X_ij) = 1 -- exactly one entry into each node

  // To avoid multiple loops, the MTZ formulation introduces a counter
  // for the nodes. Assuming i starts from 1
  // U_1 = 1
  // Uj - Ui = 1 iff (X_ij =1)
  // which can be defined as
  // U_i - U_j + 1 <= (n-1)(1-X_ij) (i in 1..n , j in 2..n, i != j)

  class Vertex(
    val x: Int,
    val y: Int,
  ) {
    // Cartesian Distance between two vertices rounded to Int values
    fun distance(remote: Vertex): Int =
      Math
        .pow(
          Math.pow(remote.x.toDouble() - x.toDouble(), 2.0) +
            Math.pow(remote.y.toDouble() - y.toDouble(), 2.0),
          0.5,
        ).roundToInt()

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as Vertex
      return x == other.x && y == other.y
    }

    override fun hashCode(): Int = "$x-$y".hashCode()

    override fun toString(): String = "(x: $x, y: $y)"
  }

  fun generateVertices(n: Int): Map<Int, Vertex> {
    val rand = fun(): Int = (0..500).random()
    val vertices = mutableSetOf<Vertex>()
    do {
      val newVertex = Vertex(x = rand(), y = rand())
      if (!vertices.contains(newVertex)) {
        vertices.add(newVertex)
      }
    } while (vertices.size < n)
    return vertices.toList().mapIndexed { index, vertex -> Pair(index, vertex) }.toMap()
  }

  fun generateModel(vertices: Map<Int, Vertex>): LPModel =
    LPModel("Travelling Salesman").apply {
      this.objective.objective = LPObjectiveType.MINIMIZE

      val n = vertices.size
      for (i in 0 until n) {
        for (j in 0 until n) {
          if (i == j) continue
          this.variables.add(LPVar("x-$i-$j", LPVarType.BOOLEAN))
          this.constants.add(LPConstant("c-$i-$j", vertices[i]!!.distance(vertices[j]!!)))
          this.objective.expression.addTerm("c-$i-$j", "x-$i-$j")
        }
        this.variables.add(LPVar("u-$i", LPVarType.INTEGER, 1, n - 1))
      }

      // constrain u-0 to 0
      this.variables.get("u-0")!!.bounds(0.0, 0.0)

      // initialize ingress / egress constraints
      for (i in 0 until n) {
        val egressConstraint =
          LPConstraint("Egress-$i").apply {
            this.operator = LPOperator.EQUAL
            this.rhs.add(1)
          }
        val ingressConstraint =
          LPConstraint("Ingress-$i").apply {
            this.operator = LPOperator.EQUAL
            this.rhs.add(1)
          }
        for (j in 0 until n) {
          if (i == j) continue
          egressConstraint.lhs.addTerm("x-$i-$j")
          ingressConstraint.lhs.addTerm("x-$j-$i")
        }
        this.constraints.add(egressConstraint)
        this.constraints.add(ingressConstraint)
      }
      // u_i - u_j + 1 <= (n-1)(1-X_ij) (i in 1..n , j in 2..n, i != j)
      for (i in 0 until n) {
        for (j in 1 until n) {
          if (i == j) continue
          val constraint =
            LPConstraint("Sequence-$i-$j").apply {
              this.lhs
                .addTerm("u-$i")
                .addTerm(-1, "u-$j")
                .add(1)
              this.operator = LPOperator.LESS_EQUAL
              this.rhs.add(n - 1).addTerm(1 - n, "x-$i-$j")
            }
          this.constraints.add(constraint)
        }
      }
    }

  @Test
  fun generateProblem() {
    val vertexMap = generateVertices(10)
    log.info { "Vertex map: $vertexMap" }
    val startVertex = vertexMap[0]!!
    val dynProbSol =
      evaluateMinCost(
        0,
        startVertex,
        startVertex,
        vertexMap.values.filter { v ->
          v != startVertex
        },
      )
    log.info { "Solution with dynamic programming: = $dynProbSol" }
    val model = generateModel(vertexMap)
    val solver = Solver.create(model)
    val ok = solver.initialize()
    assertTrue(ok, "solver.initialize() want true got false")
    val status = solver.solve()
    assertEquals(LPSolutionStatus.OPTIMAL, status, "solver.solve() want OPTIMAL got $status")
    log.info { model.solution }
    assertNotNull(model.solution, "Model should be computed successfully.")
    val objeciveFromILP = model.solution!!.objective!!
    assertEquals(dynProbSol, objeciveFromILP.roundToInt(), "ilp.objective want $dynProbSol got $objeciveFromILP")
  }

  fun evaluateMinCost(
    currentCost: Int,
    currentVertex: Vertex,
    terminatingVertex: Vertex,
    vertices: List<Vertex>,
  ): Int {
    if (vertices.isEmpty()) {
      return currentCost + terminatingVertex.distance(currentVertex)
    }
    return vertices.minOfOrNull { v ->
      val totalCost =
        evaluateMinCost(
          currentCost + currentVertex.distance(v),
          v,
          terminatingVertex,
          vertices.stream().filter { vertex -> !vertex.equals(v) }.toList(),
        )
      totalCost
    }!!
  }
}