package io.github.mohitc.lpsolver

import io.github.mohitc.lpapi.model.LPModel
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean

abstract class LPSolver<T>(
  val model: LPModel,
) : AutoCloseable {
  val log = KotlinLogging.logger(this.javaClass.simpleName)
  private val isClosed = AtomicBoolean(false)

  protected fun checkOpen() {
    if (isClosed.get()) {
      throw RuntimeException("${this.javaClass.simpleName} is closed")
    }
  }

  override fun close() {
    if (isClosed.compareAndSet(false, true)) {
      free()
    }
  }

  /**
   * Frees the resources associated with the solver.
   * Subclasses should implement this method to perform cleanup.
   */
  protected abstract fun free()

  /** Function to initialize the model in the solver based on the model specification in the LPModel
   * model
   */
  fun initialize(): Boolean {
    checkOpen()
    try {
      if (!initModel() || !initVars() || !initConstraints()) {
        return false
      }
      log.error { "Initializing Objective Function" }
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

  companion object {
    /** Function to log system information while loading system libraries.
     */
    fun logSystemInformation(): String =
      (
        """
            java.library.path: ${System.getProperty("java.library.path")}
            java.vendor: ${System.getProperty("java.vendor")}
            java.version: ${System.getProperty("java.version")}
            java.vm.name: ${System.getProperty("java.vm.name")}
            java.vm.version: ${System.getProperty("java.vm.version")}
            java.runtime.version: ${System.getProperty("java.runtime.version")}
          """
      )
  }
}