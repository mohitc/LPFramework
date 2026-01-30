package io.github.mohitc.scip.ffm

import mu.KotlinLogging
import org.scip.java.SCIP
import org.scip.java.SCIPConstraint
import org.scip.java.SCIPParams
import org.scip.java.SCIPPlugins
import org.scip.java.SCIPReleaseConstraint
import java.lang.foreign.Arena
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.SequenceLayout
import java.lang.foreign.ValueLayout
import java.util.concurrent.atomic.AtomicBoolean

class SCIPProblem : AutoCloseable {
  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  private val scipPtr: MemorySegment
  private val scipArena: Arena = Arena.ofConfined()
  private val isClosed: AtomicBoolean

  constructor() {
    val scipPtrPtr: MemorySegment = scipArena.allocate(SCIP.C_POINTER)
    val retCode = SCIPRetCode.fromVal(SCIP.SCIPcreate(scipPtrPtr))
    if (retCode != SCIPRetCode.SCIP_OKAY) {
      throw RuntimeException("SCIPcreate() failed with return code $retCode")
    }
    this.scipPtr = getPtrFromPtrPtr(scipPtrPtr)
    this.isClosed = AtomicBoolean(false)
  }

  companion object {
    fun getPtrFromPtrPtr(varPtrPtr: MemorySegment): MemorySegment = varPtrPtr.get(SCIP.C_POINTER, 0)

    fun getPtrPtrFromPtr(
      arena: Arena,
      varPtr: MemorySegment,
    ): MemorySegment {
      val varPtrPtr = arena.allocateFrom(SCIP.C_POINTER, varPtr)
      return varPtrPtr
    }

    fun createPtrArray(
      it: Arena,
      vars: List<Variable>,
    ): MemorySegment {
      val layout: SequenceLayout = MemoryLayout.sequenceLayout(vars.size.toLong(), SCIP.C_POINTER)
      val arraySegment = it.allocate(layout)
      for (i in vars.indices) {
        arraySegment.setAtIndex(SCIP.C_POINTER, i.toLong(), vars[i].ptr)
      }
      return arraySegment
    }
  }

  private fun checkOpen() {
    if (isClosed.get()) {
      throw RuntimeException("SCIPProblem is closed")
    }
  }

  override fun close() {
    if (isClosed.compareAndSet(false, true)) {
      SCIPRetCode.fromVal(SCIP.SCIPfreeProb(scipPtr))
    }
  }

  fun infinity(): Double {
    checkOpen()
    return SCIP.SCIPinfinity(scipPtr)
  }

  fun createProblem(probName: String): SCIPRetCode {
    checkOpen()
    return Arena.ofConfined().use {
      SCIPRetCode.fromVal(SCIP.SCIPcreateProbBasic(scipPtr, it.allocateFrom(probName)))
    }
  }

  fun getStatus(): SCIPStatus {
    checkOpen()
    return SCIPStatus.fromVal(SCIP.SCIPgetStatus(scipPtr))
  }

  fun includeDefaultPlugins(): SCIPRetCode {
    checkOpen()
    return SCIPRetCode.fromVal(SCIPPlugins.SCIPincludeDefaultPlugins(scipPtr))
  }

  fun createVar(
    varName: String,
    lb: Double,
    ub: Double,
    objective: Double,
    varType: SCIPVarType,
  ): Variable? {
    checkOpen()
    Arena.ofConfined().use {
      val varPtrPtr: MemorySegment = scipArena.allocate(SCIP.C_POINTER) // VAR**
      var retCode =
        SCIPRetCode.fromVal(
          SCIP.SCIPcreateVarBasic(scipPtr, varPtrPtr, it.allocateFrom(varName), lb, ub, objective, varType.value),
        )
      if (retCode != SCIPRetCode.SCIP_OKAY) {
        log.error { "Error while creating variable $varName: $retCode" }
        return null
      }
      val varPtr = getPtrFromPtrPtr(varPtrPtr)
      retCode = SCIPRetCode.fromVal(SCIP.SCIPaddVar(scipPtr, varPtr))
      if (retCode != SCIPRetCode.SCIP_OKAY) {
        log.error { "Error while adding variable $varName to model: $retCode" }
        return null
      }
      return Variable(ptr = varPtr)
    }
  }

  fun releaseVar(scipVar: Variable): SCIPRetCode {
    checkOpen()
    return Arena.ofConfined().use {
      SCIPRetCode.fromVal(SCIP.SCIPreleaseVar(scipPtr, getPtrPtrFromPtr(it, scipVar.ptr)))
    }
  }

  fun createConstraint(
    name: String,
    vars: List<Variable>,
    coefficients: List<Double>,
    lb: Double,
    ub: Double,
  ): Constraint? {
    checkOpen()
    Arena.ofConfined().use {
      val constraintPtrPtr = scipArena.allocate(SCIP.C_POINTER)
      var retCode =
        SCIPRetCode.fromVal(
          SCIPConstraint.SCIPcreateConsBasicLinear(
            scipPtr,
            constraintPtrPtr,
            it.allocateFrom(name),
            vars.size,
            createPtrArray(it, vars),
            it.allocateFrom(ValueLayout.JAVA_DOUBLE, *(coefficients.toDoubleArray())),
            lb,
            ub,
          ),
        )
      if (retCode != SCIPRetCode.SCIP_OKAY) {
        log.error { "Error while creating constraint ($name) : $retCode" }
        return null
      }
      val constraintPtr = getPtrFromPtrPtr(constraintPtrPtr)
      retCode = SCIPRetCode.fromVal(SCIP.SCIPaddCons(scipPtr, constraintPtr))
      if (retCode != SCIPRetCode.SCIP_OKAY) {
        log.error { "Error while adding constraint ($name) to model : $retCode" }
        return null
      }
      return Constraint(ptr = constraintPtr)
    }
  }

  fun releaseConstraint(constraint: Constraint): SCIPRetCode {
    checkOpen()
    return Arena.ofConfined().use {
      SCIPRetCode.fromVal(SCIPReleaseConstraint.SCIPreleaseCons(scipPtr, getPtrPtrFromPtr(it, constraint.ptr)))
    }
  }

  fun setBoolParam(
    param: String,
    value: Boolean,
  ): SCIPRetCode {
    checkOpen()
    return Arena.ofConfined().use {
      SCIPRetCode.fromVal(SCIPParams.SCIPsetBoolParam(scipPtr, it.allocateFrom(param), if (value) 1 else 0))
    }
  }

  fun setRealParam(
    param: String,
    value: Double,
  ): SCIPRetCode {
    checkOpen()
    return Arena.ofConfined().use {
      SCIPRetCode.fromVal(SCIPParams.SCIPsetRealParam(scipPtr, it.allocateFrom(param), value))
    }
  }

  fun setStringParam(
    param: String,
    value: String,
  ): SCIPRetCode {
    checkOpen()
    return Arena.ofConfined().use {
      SCIPRetCode.fromVal(SCIPParams.SCIPsetStringParam(scipPtr, it.allocateFrom(param), it.allocateFrom(value)))
    }
  }

  fun setIntParam(
    param: String,
    value: Int,
  ): SCIPRetCode {
    checkOpen()
    return Arena.ofConfined().use {
      SCIPRetCode.fromVal(SCIPParams.SCIPsetIntParam(scipPtr, it.allocateFrom(param), value))
    }
  }

  fun setLongintParam(
    param: String,
    value: Long,
  ): SCIPRetCode {
    checkOpen()
    return Arena.ofConfined().use {
      SCIPRetCode.fromVal(SCIPParams.SCIPsetLongintParam(scipPtr, it.allocateFrom(param), value))
    }
  }

  fun solve(): SCIPRetCode {
    checkOpen()
    return SCIPRetCode.fromVal(SCIP.SCIPsolve(scipPtr))
  }

  fun getGap(): Double {
    checkOpen()
    return SCIP.SCIPgetGap(scipPtr)
  }

  fun getBestSol(): Solution? {
    checkOpen()
    val sol = SCIP.SCIPgetBestSol(scipPtr)
    log.info { "Best Solution $sol" }
    return if (sol != null && sol.address() != 0L) Solution(ptr = sol) else null
  }

  fun getSolVal(
    sol: Solution,
    variable: Variable,
  ): Double {
    checkOpen()
    return SCIP.SCIPgetSolVal(scipPtr, sol.ptr, variable.ptr)
  }

  fun setVariableObjective(
    variable: Variable,
    objective: Double,
  ): SCIPRetCode {
    checkOpen()
    return SCIPRetCode.fromVal(SCIP.SCIPchgVarObj(scipPtr, variable.ptr, objective))
  }

  fun maximize(): SCIPRetCode {
    checkOpen()
    return SCIPRetCode.fromVal(SCIP.SCIPsetObjsense(scipPtr, SCIP.SCIP_OBJSENSE_MAXIMIZE()))
  }

  fun minimize(): SCIPRetCode {
    checkOpen()
    return SCIPRetCode.fromVal(SCIP.SCIPsetObjsense(scipPtr, SCIP.SCIP_OBJSENSE_MINIMIZE()))
  }

  fun messageHandlerQuiet(value: Boolean) {
    checkOpen()
    val setting: Int = if (value) 1 else 0
    SCIP.SCIPsetMessagehdlrQuiet(scipPtr, setting)
  }

  fun writeOriginalProblem(
    fileName: String,
    extension: String,
    genericNames: SCIPBool,
  ): SCIPRetCode {
    checkOpen()
    return Arena.ofConfined().use {
      SCIPRetCode.fromVal(
        SCIP.SCIPwriteOrigProblem(scipPtr, it.allocateFrom(fileName), it.allocateFrom(extension), genericNames.value),
      )
    }
  }
}

open class CPointer(
  val ptr: MemorySegment,
)

class Variable(
  ptr: MemorySegment,
) : CPointer(ptr)

class Constraint(
  ptr: MemorySegment,
) : CPointer(ptr)

class Solution(
  ptr: MemorySegment,
) : CPointer(ptr)