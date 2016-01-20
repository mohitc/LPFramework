package com.lpapi.entities.skeleton.impl;

import com.lp.skeleton.entities.SkeletonConstr;
import com.lpapi.entities.*;
import com.lpapi.exception.LPConstraintException;

public class SkeletonLPConstraintFactory implements LPConstraintFactory<SkeletonConstr> {

  @Override
  public LPConstraint<SkeletonConstr> generateConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    return new SkeletonLPConstraint(model, identifier, lhs, operator, rhs);
  }
}
