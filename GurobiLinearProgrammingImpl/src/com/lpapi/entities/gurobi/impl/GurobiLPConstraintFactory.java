package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPConstraintException;
import gurobi.GRBConstr;

public class GurobiLPConstraintFactory implements LPConstraintFactory<GRBConstr> {

  @Override
  public LPConstraint<GRBConstr> generateConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    return new GurobiLPConstraint(model, identifier, lhs, operator, rhs);
  }
}
