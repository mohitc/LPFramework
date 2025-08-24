package io.github.mohitc.lpsolver.sample

import io.github.mohitc.lpapi.model.LPConstraint
import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.LPVar
import io.github.mohitc.lpapi.model.enums.LPOperator
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import io.github.mohitc.lpapi.model.enums.LPVarType
import io.github.mohitc.lpsolver.spi.Solver
import io.github.mohitc.lpsolver.test.SolverTestInstance
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals

class SudokuSolverSample : SolverTestInstance {
  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  private var model: LPModel? = null

  override fun name(): String = "Sudoku Solver"

  private fun varName(
    fieldVal: Int,
    x: Int,
    y: Int,
  ): String = "$fieldVal-$x-$y"

  private fun baseModelGenerator(): LPModel {
    val model = LPModel("sudoku")

    // variables for the Sudoku model are indicated as boolean variables 1-x-y, 2-x-y to indicate if the (x,y) coord
    // is (1, 2 ... 9)

    log.info { "Initializing boolean variables" }
    for (fieldVal in 1..9) {
      for (x in 1..9) {
        for (y in 1..9) {
          model.variables.add(LPVar(varName(fieldVal, x, y), LPVarType.BOOLEAN))
        }
      }
    }

    // Initialize constraint that each coordinate can only have a single value set at a time
    for (x in 1..9) {
      for (y in 1..9) {
        log.debug { "Initialize constraint to indicate that coordinate ($x, $y) has exactly one field value" }
        val constr = LPConstraint("UniqueValue-$x-$y")
        for (fieldVal in 1..9) {
          constr.lhs.addTerm(varName(fieldVal, x, y))
        }
        constr.operator = LPOperator.EQUAL
        constr.rhs.add(1)
        model.constraints.add(constr)
      }
    }

    // Initialize row and column constraints
    for (fieldVal in 1..9) {
      for (x in 1..9) {
        log.debug { "Initializing row and column constraints for variable $fieldVal and row/column $x" }
        val rowConstraint = LPConstraint("row-($fieldVal)-$x")
        val columnConstraint = LPConstraint("column-($fieldVal)-$x")
        for (y in 1..9) {
          rowConstraint.lhs.addTerm(varName(fieldVal, x, y))
          columnConstraint.lhs.addTerm(varName(fieldVal, y, x))
        }
        rowConstraint.operator = LPOperator.EQUAL
        columnConstraint.operator = LPOperator.EQUAL
        rowConstraint.rhs.add(1)
        columnConstraint.rhs.add(1)
        model.constraints.add(rowConstraint)
        model.constraints.add(columnConstraint)
      }
    }

    // Initializing grid constraint
    for (startX in 1..8 step 3) {
      for (startY in 1..9 step 3) {
        for (fieldVal in 1..9) {
          log.debug { "Adding constraint for variable $fieldVal in grid starting at ($startX, $startY)" }
          val constr = LPConstraint("gridconstr-$fieldVal-$startX-$startY")
          for (x in startX..(startX + 2)) {
            for (y in startY..(startY + 2)) {
              constr.lhs.addTerm(varName(fieldVal, x, y))
            }
          }
          constr.operator = LPOperator.EQUAL
          constr.rhs.add(1)
          model.constraints.add(constr)
        }
      }
    }

    // For sudoku, we just need a feasible solution so no objective function change is required.
    return model
  }

  override fun initModel(): Boolean {
    val m = baseModelGenerator()
    // Initialize feasible sudoku problem instance
    // __|_1_2_3_4_5_6_7_8_9
    // 1 | - 5 - 8 - - - - 6
    // 2 | 4 - - - - - 2 - 5
    // 3 | 6 - - - - - - - -
    // 4 | - - 2 - - - 1 - -
    // 5 | 7 - - 9 - 6 4 - -
    // 6 | - - - - - - - 5 -
    // 7 | 2 - 9 5 - - 7 3 -
    // 8 | - - - - 1 - - - 4
    // 9 | - 3 - - 8 - - - -

    mutableListOf(
      varName(5, 2, 1),
      varName(8, 4, 1),
      varName(6, 9, 1),
      varName(4, 1, 2),
      varName(2, 7, 2),
      varName(5, 9, 2),
      varName(6, 1, 3),
      varName(2, 3, 4),
      varName(1, 7, 4),
      varName(7, 1, 5),
      varName(9, 4, 5),
      varName(6, 6, 5),
      varName(4, 7, 5),
      varName(5, 8, 6),
      varName(2, 1, 7),
      varName(9, 3, 7),
      varName(5, 4, 7),
      varName(7, 7, 7),
      varName(3, 8, 7),
      varName(1, 5, 8),
      varName(4, 9, 8),
      varName(3, 2, 9),
      varName(8, 5, 9),
    ).mapNotNull { v -> m.variables.get(v) }.forEach { v ->
      v.bounds(1.0, 1.0)
    }

    model = m
    return m.validate()
  }

  override fun solveAndValidate() {
    log.info { "Initializing the solver " }
    val solver = Solver.create(model!!)
    solver.initialize()
    val status = solver.solve()
    log.info { model!!.solution }
    assertEquals(status, LPSolutionStatus.OPTIMAL, "model should be solved successfully")
  }
}