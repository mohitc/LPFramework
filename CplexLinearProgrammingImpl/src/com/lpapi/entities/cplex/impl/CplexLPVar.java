package com.lpapi.entities.cplex.impl;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarType;
import com.lpapi.exception.LPModelException;
import com.lpapi.exception.LPVarException;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

public class CplexLPVar extends LPVar<IloNumVar> {

  IloNumVar modelVar;

  protected CplexLPVar(LPModel model, String identifier, LPVarType type) throws LPVarException {
    super(model, identifier, type);
  }

  @Override
  public IloNumVar getModelVar() {
    return modelVar;
  }

  @Override
  protected void initModelVar() throws LPModelException {
    LPModel lpModel = this.getModel();
    if (lpModel.getModel()!=null) {
      if (lpModel.getModel().getClass().isAssignableFrom(IloCplex.class)) {
        try {
          modelVar = ((IloCplex)lpModel.getModel()).numVar(this.getlBound(), this.getuBound(), getCplexVarType(), this.getIdentifier());
        } catch (IloException e) {
          log.error("Error in creating CPLEX variable", e);
        }
      }
    }
  }

  private IloNumVarType getCplexVarType() throws LPModelException{
    LPVarType type = this.getVarType();
    switch (type) {
      case INTEGER: return IloNumVarType.Int;
      case BOOLEAN: return IloNumVarType.Bool;
      case DOUBLE:  return IloNumVarType.Float;
    }
    throw new LPModelException("No variable type defined for variable: " + this.getIdentifier());
  }

  public String toString () {
    return super.toString();
  }
}
