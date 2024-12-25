package com.lpapi.entities.glpk.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPConstraintException;

public class GlpkLPConstraintFactory implements LPConstraintFactory<Integer> {
  @Override
  public LPConstraint<Integer> generateConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    if (model !=null && model instanceof GlpkLPModel) {
      return new GlpkLPConstraint(model, identifier, lhs, operator, rhs);
    }
    throw new LPConstraintException("Incompatible model type for generating GlpkLPConstraint");
  }
}
