package com.lpapi.entities.skeleton.impl;

import com.lp.skeleton.entities.SkeletonVar;
import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarFactory;
import com.lpapi.entities.LPVarType;
import com.lpapi.exception.LPVarException;

public class SkeletonLPVarFactory implements LPVarFactory<SkeletonVar> {

  @Override
  public LPVar<SkeletonVar> generateLPVar(LPModel model, String identifier, LPVarType type, double lBound, double uBound) throws LPVarException {
    SkeletonLPVar var = new SkeletonLPVar(model, identifier, type);
    var.setBounds(lBound,uBound);
    return var;
  }
}
