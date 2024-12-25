package com.lpapi.entities.cplex.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPConstraintException;
import com.lpapi.exception.LPModelException;
import ilog.concert.*;

public class CplexLPConstraint extends LPConstraint<IloConstraint> {

  IloConstraint modelConstr;

  private CplexLPModel model;

  private CplexLPExpressionGenerator exprGenerator;

  public CplexLPConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    super(model, identifier, lhs, operator, rhs);
    if (!CplexLPModel.class.isAssignableFrom(model.getClass())) {
      throw new LPConstraintException("Model should be of type CplexLPModel");
    }
    this.model = (CplexLPModel)model;
  }

  @Override
  protected IloConstraint getModelConstraint() {
    return modelConstr;
  }

  @Override
  protected void initModelConstraint() throws LPModelException {
    exprGenerator = new CplexLPExpressionGenerator(model.getModel());
    try {
      switch (this.getOperator()) {
        case LESS_EQUAL:
          this.modelConstr = model.getModel().addLe(exprGenerator.generateExpression(getLhs()), exprGenerator.generateExpression(getRhs()), getIdentifier());
          break;
        case GREATER_EQUAL:
          this.modelConstr = model.getModel().addGe(exprGenerator.generateExpression(getLhs()), exprGenerator.generateExpression(getRhs()), getIdentifier());
          break;
        case EQUAL:
          this.modelConstr = model.getModel().addEq(exprGenerator.generateExpression(getLhs()), exprGenerator.generateExpression(getRhs()), getIdentifier());
          break;
      }
    } catch (IloException e) {
      log.error("Error while generating Cplex constraint", e);
      throw new LPModelException("Error in generating model constraint: " + e.getMessage());
    }
  }

}
