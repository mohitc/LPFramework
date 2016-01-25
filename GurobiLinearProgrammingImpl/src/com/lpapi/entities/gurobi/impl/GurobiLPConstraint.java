package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPConstraintException;
import com.lpapi.exception.LPModelException;
import gurobi.*;

public class GurobiLPConstraint extends LPConstraint<GRBConstr> {

  GRBConstr modelConstr;

  private  GurobiLPModel model;

  private GurobiLPExpressionGenerator exprGen;

  public GurobiLPConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    super(model, identifier, lhs, operator, rhs);
    if (!GurobiLPModel.class.isAssignableFrom(model.getClass())) {
      throw new LPConstraintException("Model should be of type GurobiLPModel");
    }
    this.model = (GurobiLPModel)model;
    exprGen = new GurobiLPExpressionGenerator(identifier);
  }

  @Override
  protected GRBConstr getModelConstraint() {
    return modelConstr;
  }

  @Override
  protected void initModelConstraint() throws LPModelException {
    try {
      this.modelConstr = model.getModel().addConstr(exprGen.generateExpression(getLhs()), getGurobiOperator(), exprGen.generateExpression(getRhs()), getIdentifier());
    } catch (GRBException e) {
      log.error("Error while generating Gurobi constraint", e);
      throw new LPModelException("Error in generating model constraint: " + e.getMessage());
    }
  }

  private char getGurobiOperator() throws LPModelException {
    switch (getOperator()) {
      case GREATER_EQUAL: return GRB.GREATER_EQUAL;
      case EQUAL:         return GRB.EQUAL;
      case LESS_EQUAL:    return GRB.LESS_EQUAL;
    }
    throw new LPModelException("Operator not defined");
  }

  public String toString () {
    return super.toString();
  }
}
