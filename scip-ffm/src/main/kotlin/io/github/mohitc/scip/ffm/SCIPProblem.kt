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

class SCIPProblem {
  private val log = KotlinLogging.logger(this.javaClass.simpleName)

  private lateinit var scipPtr: MemorySegment

  private val scipArena: Arena = Arena.ofConfined()

  init {
    initialize()
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

  private fun initialize(): SCIPRetCode {
    val compilerAddress: MemorySegment = scipArena.allocate(SCIP.C_POINTER) // SCIP**
    val retCode: Int = SCIP.SCIPcreate(compilerAddress)
    scipPtr = getPtrFromPtrPtr(compilerAddress)
    return SCIPRetCode.fromVal(retCode)
  }

  fun infinity() = SCIP.SCIPinfinity(scipPtr)

  fun createProblem(probName: String) =
    Arena.ofConfined().use {
      SCIPRetCode.fromVal(SCIP.SCIPcreateProbBasic(scipPtr, it.allocateFrom(probName)))
    }

  fun getStatus() = SCIPStatus.fromVal(SCIP.SCIPgetStatus(scipPtr))

  fun freeProblem() = SCIPRetCode.fromVal(SCIP.SCIPfreeProb(scipPtr))

  fun includeDefaultPlugins() = SCIPRetCode.fromVal(SCIPPlugins.SCIPincludeDefaultPlugins(scipPtr))

  fun createVar(
    varName: String,
    lb: Double,
    ub: Double,
    objective: Double,
    varType: SCIPVarType,
  ): Variable? {
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

  fun releaseVar(scipVar: Variable) =
    Arena.ofConfined().use { SCIPRetCode.fromVal(SCIP.SCIPreleaseVar(scipPtr, getPtrPtrFromPtr(it, scipVar.ptr))) }

  fun createConstraint(
    name: String,
    vars: List<Variable>,
    coefficients: List<Double>,
    lb: Double,
    ub: Double,
  ): Constraint? {
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

  fun releaseConstraint(constraint: Constraint) =
    Arena.ofConfined().use {
      SCIPRetCode.fromVal(SCIPReleaseConstraint.SCIPreleaseCons(scipPtr, getPtrPtrFromPtr(it, constraint.ptr)))
    }

  fun setBoolParam(
    param: String,
    value: Boolean,
  ) = Arena.ofConfined().use {
    SCIPRetCode.fromVal(SCIPParams.SCIPsetBoolParam(scipPtr, it.allocateFrom(param), if (value) 1 else 0))
  }

  fun setRealParam(
    param: String,
    value: Double,
  ) = Arena.ofConfined().use {
    SCIPRetCode.fromVal(SCIPParams.SCIPsetRealParam(scipPtr, it.allocateFrom(param), value))
  }

  fun setStringParam(
    param: String,
    value: String,
  ) = Arena.ofConfined().use {
    SCIPRetCode.fromVal(SCIPParams.SCIPsetStringParam(scipPtr, it.allocateFrom(param), it.allocateFrom(value)))
  }

  fun setIntParam(
    param: String,
    value: Int,
  ) = Arena.ofConfined().use {
    SCIPRetCode.fromVal(SCIPParams.SCIPsetIntParam(scipPtr, it.allocateFrom(param), value))
  }

  fun setLongintParam(
    param: String,
    value: Long,
  ) = Arena.ofConfined().use {
    SCIPRetCode.fromVal(SCIPParams.SCIPsetLongintParam(scipPtr, it.allocateFrom(param), value))
  }

  fun solve() = SCIPRetCode.fromVal(SCIP.SCIPsolve(scipPtr))

  fun getGap() = SCIP.SCIPgetGap(scipPtr)

  fun getBestSol(): Solution? {
    val sol = SCIP.SCIPgetBestSol(scipPtr)
    log.info { "Best Solution $sol" }
    return if (sol != null && sol.address() != 0L) Solution(ptr = sol) else null
  }

  fun getSolVal(
    sol: Solution,
    variable: Variable,
  ) = SCIP.SCIPgetSolVal(scipPtr, sol.ptr, variable.ptr)

  fun setVariableObjective(
    variable: Variable,
    objective: Double,
  ) = SCIPRetCode.fromVal(SCIP.SCIPchgVarObj(scipPtr, variable.ptr, objective))

  fun maximize() = SCIP.SCIPsetObjsense(scipPtr, SCIP.SCIP_OBJSENSE_MAXIMIZE())

  fun minimize() = SCIP.SCIPsetObjsense(scipPtr, SCIP.SCIP_OBJSENSE_MINIMIZE())

  fun messageHandlerQuiet(value: Boolean) {
    val setting: Int = if (value) 1 else 0
    SCIP.SCIPsetMessagehdlrQuiet(scipPtr, setting)
  }

  fun writeOriginalProblem(
    fileName: String,
    extension: String,
    genericNames: SCIPBool,
  ) = Arena.ofConfined().use {
    SCIP.SCIPwriteOrigProblem(scipPtr, it.allocateFrom(fileName), it.allocateFrom(extension), genericNames.value)
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