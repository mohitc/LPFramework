package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarType;
import com.lpapi.exception.LPModelException;
import com.lpapi.exception.LPVarException;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class GurobiLPVar extends LPVar<GRBVar> {

  private GRBVar modelVar;

  protected GurobiLPVar(LPModel model, String identifier, LPVarType type) throws LPVarException {
    super(model, identifier, type);
  }

  @Override
  public GRBVar getModelVar() {
    return modelVar;
  }

  @Override
  protected void initModelVar() throws LPModelException {
    LPModel lpModel = this.getModel();
    if (lpModel.getModel()!=null) {
      if (lpModel.getModel().getClass().isAssignableFrom(GRBModel.class)) {
        try {
          modelVar = ((GRBModel)lpModel.getModel()).addVar(this.getlBound(), this.getuBound(), 0, getGrbVarType(), this.getIdentifier());
        } catch (GRBException e) {
          log.error("Error in creating Gurobi variable", e);
        }
      }
    }
  }

  private char getGrbVarType() throws LPModelException{
    LPVarType type = this.getVarType();
    switch (type) {
      case INTEGER: return GRB.INTEGER;
      case BOOLEAN: return GRB.BINARY;
      case DOUBLE:  return GRB.CONTINUOUS;
    }
    throw new LPModelException("No variable type defined for variable: " + this.getIdentifier());
  }

  public String toString () {
    return super.toString();
  }
}
