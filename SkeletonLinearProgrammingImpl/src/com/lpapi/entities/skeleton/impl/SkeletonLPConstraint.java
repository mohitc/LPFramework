package com.lpapi.entities.skeleton.impl;

import com.lp.skeleton.entities.SkeletonConstr;
import com.lpapi.entities.LPConstraint;
import com.lpapi.entities.LPExpression;
import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPOperator;
import com.lpapi.exception.LPConstraintException;
import com.lpapi.exception.LPModelException;

public class SkeletonLPConstraint extends LPConstraint<SkeletonConstr> {

  SkeletonConstr modelConstr;

  private SkeletonLPExpressionGenerator exprGen;

  public SkeletonLPConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    super(model, identifier, lhs, operator, rhs);
    exprGen = new SkeletonLPExpressionGenerator();
  }

  @Override
  protected SkeletonConstr getModelConstraint() {
    return modelConstr;
  }

  @Override
  protected void initModelConstraint() throws LPModelException {
    log.debug("Do nothing");
  }

  public String toString () {
    return super.toString();
  }
}
