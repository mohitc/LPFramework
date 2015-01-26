package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarType;
import com.lpapi.entities.exception.LPModelException;
import com.lpapi.entities.exception.LPVarException;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class GurobiLPVar extends LPVar<GRBVar> {

  private GRBVar modelVar;

  protected GurobiLPVar(LPModel model, String identifier, LPVarType type, GRBVar modelVar) throws LPVarException {
    super(model, identifier, type);
    if (modelVar!=null)
      this.modelVar = modelVar;
    else
      throw new LPVarException("Model variable cannot be null");
  }

  @Override
  public GRBVar getModelVar() {
    return modelVar;
  }

  @Override
  protected void initModelVar(GRBVar var) throws LPModelException {
    LPModel lpModel = this.getModel();
    if (lpModel.getModel()!=null) {
      if (lpModel.getModel().getClass().isAssignableFrom(GRBModel.class)) {
      }
    }
  }

}
