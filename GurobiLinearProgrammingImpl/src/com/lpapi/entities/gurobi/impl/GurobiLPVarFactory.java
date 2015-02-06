package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarFactory;
import com.lpapi.entities.LPVarType;
import com.lpapi.exception.LPVarException;
import gurobi.GRBVar;

public class GurobiLPVarFactory implements LPVarFactory<GRBVar> {

  @Override
  public LPVar<GRBVar> generateLPVar(LPModel model, String identifier, LPVarType type, double lBound, double uBound) throws LPVarException {
    GurobiLPVar var = new GurobiLPVar(model, identifier, type);
    var.setBounds(lBound,uBound);
    return var;
  }
}
