package io.github.mohitc.glpk.ffm

import org.glpk.java.glp_iocp
import java.lang.foreign.MemorySegment

data class GlpIocp(
  var messageLevel: GLPKMessageLevel = GLPKMessageLevel.MSG_OFF,
  var simpleRoundingHeuristic: GLPKFeatureStatus = GLPKFeatureStatus.OFF,
  var feasibilityPumpHeuristic: GLPKFeatureStatus = GLPKFeatureStatus.OFF,
  var preSolve: GLPKFeatureStatus = GLPKFeatureStatus.OFF,
  var binarize: GLPKFeatureStatus = GLPKFeatureStatus.OFF,
) {
  fun apply(cStruct: MemorySegment) {
    glp_iocp.msg_lev(cStruct, messageLevel.value)
    glp_iocp.presolve(cStruct, preSolve.value)
    glp_iocp.binarize(cStruct, binarize.value)
  }
}