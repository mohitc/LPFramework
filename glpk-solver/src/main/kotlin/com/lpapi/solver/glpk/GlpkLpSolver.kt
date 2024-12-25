package com.lpapi.solver.glpk

import com.lpapi.model.LPModel
import com.lpapi.model.enums.LPVarType
import com.lpapi.solver.LPSolver
import com.lpapi.solver.enums.LPSolutionStatus
import mu.KotlinLogging
import org.gnu.glpk.GLPK
import org.gnu.glpk.GLPKConstants
import org.gnu.glpk.glp_prob

class GlpkLpSolver : LPSolver<glp_prob> {

  val log = KotlinLogging.logger("GlpkLpSolver")

  var glpkModel : glp_prob? = null

  private var variableMap : MutableMap<String, Int> = mutableMapOf()

  override fun initialize(model: LPModel) {
    try {
      glpkModel = GLPK.glp_create_prob()
      GLPK.glp_set_prob_name(glpkModel, model.identifier)
      initVars(model)
    } catch (e: Exception) {
      log.error { "Error while initializing GLPK Problem instance $e" }
    }
  }

  override fun getBaseModel(): glp_prob? {
    return glpkModel
  }

  override fun solve(): LPSolutionStatus {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }


  /** Function to initialize all variables in the
   *
   */
  private fun initVars(model: LPModel) {
    log.info { "Initializing variables" }
    model.variables.allValues().forEach {
      try {
        log.debug { "Initializing variable ($it)" }
        val index: Int = GLPK.glp_add_cols(glpkModel, 1)
        variableMap[it.identifier] = index
        GLPK.glp_set_col_name(glpkModel, index, it.identifier)
        GLPK.glp_set_col_kind(glpkModel, index, getGlpVarType(it.type))
        val boundType: Int = if (it.lbound == it.ubound) GLPKConstants.GLP_FX else GLPKConstants.GLP_BV
        GLPK.glp_set_col_bnds(glpkModel, index, boundType, it.lbound, it.ubound)
      } catch (e: Exception) {
        log.error { "Error while initializing GLPK Var ($it) : $e" }
      }
    }
  }

  private fun getGlpVarType(type: LPVarType) : Int {
    return when (type) {
      LPVarType.BOOLEAN -> GLPKConstants.GLP_BV
      LPVarType.INTEGER -> GLPKConstants.GLP_IV
      LPVarType.DOUBLE -> GLPKConstants.GLP_CV
      else -> GLPKConstants.GLP_CV
    }
  }


}