package com.lpapi.spi

import com.lpapi.model.LPModel
import com.lpapi.solver.LPSolver
import java.util.ServiceLoader

/** LpSpi is the Service provider interface that implementations of the LPSolver can implement to automatically pick up
 * a solver from the classpath. If a solver with the LpSpi is provided in the classpath, the Solver singleton can be
 * used to generate an instance of the solver without specifying the type of the solver explicitly.
 */
interface LPSpi<T> {
  fun create(model: LPModel): LPSolver<T>
}

/** Solver is a singleton created to lookup instances of the LPSpi in the classpath using the Java Service Provider
 * Interface (https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html).
 */
object Solver {
  private var loader = ServiceLoader.load(LPSpi::class.java)

  private const val SOLVER_NOT_FOUND = "No valid solver found in classpath. Please check imports"

  /** Function to look up SPI from the classpath and pick one to initialize a solver. In case no SPIs are found, the
   * method throws a RuntimeException
   */
  fun create(model: LPModel): LPSolver<*> {
    return loader.findFirst().map { spi -> spi.create(model) }.orElseThrow { RuntimeException(SOLVER_NOT_FOUND) }
  }
}