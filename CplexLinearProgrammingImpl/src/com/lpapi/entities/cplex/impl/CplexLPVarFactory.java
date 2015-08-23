package com.lpapi.entities.cplex.impl;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarFactory;
import com.lpapi.entities.LPVarType;
import com.lpapi.exception.LPVarException;
import ilog.concert.IloNumVar;

public class CplexLPVarFactory implements LPVarFactory<IloNumVar> {

  @Override
  public LPVar<IloNumVar> generateLPVar(LPModel model, String identifier, LPVarType type, double lBound, double uBound) throws LPVarException {
    CplexLPVar var = new CplexLPVar(model, identifier, type);
    var.setBounds(lBound,uBound);
    return var;
  }
}
