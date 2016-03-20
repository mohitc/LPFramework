package com.lpapi.entities.skeleton.impl;

import com.lp.skeleton.entities.SkeletonConstr;
import com.lp.skeleton.entities.SkeletonModel;
import com.lp.skeleton.entities.SkeletonVar;
import com.lpapi.entities.*;
import com.lpapi.exception.LPModelException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SkeletonLPModel extends LPModel<SkeletonModel, SkeletonVar, SkeletonConstr> {

  private SkeletonModel model;

  private LPVarFactory<SkeletonVar> grbVarFactory;

  private LPConstraintFactory<SkeletonConstr> grbConstraintFactory;

  public SkeletonLPModel(String identifier) throws LPModelException {
    super(identifier);
    grbVarFactory = new SkeletonLPVarFactory();
    grbConstraintFactory = new SkeletonLPConstraintFactory();
  }

  @Override
  public SkeletonModel getModel() {
    return model;
  }

  @Override
  protected LPVarFactory<SkeletonVar> getLPVarFactory() {
    return grbVarFactory;
  }

  @Override
  protected LPConstraintFactory<SkeletonConstr> getLPConstraintFactory() {
    return grbConstraintFactory;
  }

  @Override
  public void initModel() throws LPModelException {
    log.debug("do nothing");
  }

  @Override
  public void initObjectiveFunction() throws LPModelException {
    log.debug("do nothing");
  }

  @Override
  public void computeModel() throws LPModelException {
    log.debug("Do nothing");
  }

  @Override
  public void extractResults() throws LPModelException {
    log.debug("Do nothing");
  }

}
