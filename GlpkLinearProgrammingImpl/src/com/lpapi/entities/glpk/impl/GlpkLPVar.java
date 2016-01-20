package com.lpapi.entities.glpk.impl;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarType;
import com.lpapi.exception.LPModelException;
import com.lpapi.exception.LPVarException;
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.glp_prob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlpkLPVar extends LPVar<Integer>{

  private static final Logger log = LoggerFactory.getLogger(GlpkLPVar.class);

  private int varIndex;

  protected GlpkLPVar(LPModel model, String identifier, LPVarType type) throws LPVarException {
    super(model, identifier, type);
  }



  @Override
  public Integer getModelVar() {
    return varIndex;
  }

  @Override
  protected void initModelVar() throws LPModelException {
    //add variable to model
    try {
      glp_prob lp = ((GlpkLPModel) getModel()).getModel();
      varIndex = GLPK.glp_add_cols(lp, 1);
      GLPK.glp_set_col_name(lp, varIndex, getIdentifier());
      GLPK.glp_set_col_kind(lp, varIndex, getGlpVarType(getVarType()));
      GLPK.glp_set_col_bnds(lp, varIndex, GLPKConstants.GLP_DB, getlBound(), getuBound());
    } catch (Exception e) {
      log.error("Error while initializing LP Variable" , e);
      throw new LPModelException("Error whilie initializing LP Variable: " + e.getMessage());
    }
  }

  private int getGlpVarType(LPVarType type) throws LPModelException {
    if (type==null) {
      throw new LPModelException("Variable type not defined");
    }
    switch (type) {
      case BOOLEAN :
        return GLPKConstants.GLP_BV;
      case INTEGER:
        return GLPKConstants.GLP_IV;
      case DOUBLE:
        return GLPKConstants.GLP_CV;
    }
    throw new LPModelException("Mapping to GLPk variable type not defined");
  }

  public String toString () {
    return super.toString();
  }
}
