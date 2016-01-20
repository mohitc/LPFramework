package com.lpapi.entities.skeleton.impl;

import com.lp.skeleton.entities.SkeletonVar;
import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarType;
import com.lpapi.exception.LPModelException;
import com.lpapi.exception.LPVarException;

public class SkeletonLPVar extends LPVar<SkeletonVar> {

  private SkeletonVar modelVar;

  protected SkeletonLPVar(LPModel model, String identifier, LPVarType type) throws LPVarException {
    super(model, identifier, type);
  }

  @Override
  public SkeletonVar getModelVar() {
    return modelVar;
  }

  @Override
  protected void initModelVar() throws LPModelException {
    log.debug("Do Nothing");
  }

  public String toString () {
    return super.toString();
  }
}
