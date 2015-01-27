package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.LPConstraintFactory;
import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVarFactory;
import com.lpapi.entities.exception.LPModelException;
import gurobi.*;

/**
 * Created by mohit on 1/28/15.
 */
public class GurobiLPModel extends LPModel<GRBModel, GRBVar, GRBConstr> {

  private GRBEnv grbEnv;

  private GRBModel model;

  private LPVarFactory<GRBVar> grbVarFactory;

  public GurobiLPModel(String identifier) throws LPModelException {
    super(identifier);
    grbVarFactory = new GurobiLPVarFactory();
  }

  @Override
  public GRBModel getModel() {
    return model;
  }

  @Override
  protected LPVarFactory<GRBVar> getLPVarFactory() {
    return grbVarFactory;
  }

  @Override
  protected LPConstraintFactory<GRBConstr> getLPConstraintFactory() {
    return null;
  }

  @Override
  public void initModel() throws LPModelException {
    try {
      grbEnv = new GRBEnv();
      model = new GRBModel(grbEnv);
    } catch (GRBException e) {
      log.error("Error in generating Gurobi model", e);
    }
  }

  @Override
  public void initConstraints() {

  }

  @Override
  public void computeModel() {

  }
}
