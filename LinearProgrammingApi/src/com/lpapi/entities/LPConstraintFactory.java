package com.lpapi.entities;

import com.lpapi.entities.exception.LPConstraintException;

public interface LPConstraintFactory<Z> {

  public LPConstraint<Z> generateConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException;
}
