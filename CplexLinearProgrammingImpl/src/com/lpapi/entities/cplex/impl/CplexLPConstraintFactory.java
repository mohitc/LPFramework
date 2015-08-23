package com.lpapi.entities.cplex.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPConstraintException;
import ilog.concert.IloConstraint;

public class CplexLPConstraintFactory implements LPConstraintFactory<IloConstraint> {

  @Override
  public LPConstraint<IloConstraint> generateConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    return new CplexLPConstraint(model, identifier, lhs, operator, rhs);
  }
}
