package com.lpapi.entities;

import com.lpapi.entities.exception.LPConstraintException;

public interface LPConstraintFactory<Z> {

  public LPConstraint<Z> generateConstraint(LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException;
}
