package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.*;
import com.lpapi.entities.exception.LPConstraintException;
import com.lpapi.entities.exception.LPModelException;
import gurobi.*;

import java.util.List;

public class GurobiLPConstraint extends LPConstraint<GRBConstr> {

  GRBConstr modelConstr;

  private  GurobiLPModel model;

  public GurobiLPConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    super(model, identifier, lhs, operator, rhs);
    if (!GurobiLPModel.class.isAssignableFrom(model.getClass())) {
      throw new LPConstraintException("Model should be of type GurobiLPModel");
    }
    this.model = (GurobiLPModel)model;
  }

  @Override
  protected GRBConstr getModelConstraint() {
    return modelConstr;
  }

  @Override
  protected void initModelConstraint() throws LPModelException {
    try {
      this.modelConstr = model.getModel().addConstr(generateLinearExpression(getLhs()), getGurobiOperator(), generateLinearExpression(getRhs()), getIdentifier());
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

  private GRBLinExpr generateLinearExpression(LPExpression expression) throws LPModelException {
    GRBLinExpr linExpr = new GRBLinExpr();
    List<LPExpressionTerm> termList = expression.getTermList();
    if ((termList!=null) && (termList.size()!=0)){
      for (LPExpressionTerm term: termList) {
        if (term.isConstant()) {
          linExpr.addConstant(term.getCoefficient());
        } else {
          if ((term.getVar().getModelVar() != null) && (GRBVar.class.isAssignableFrom(term.getVar().getModelVar().getClass()))) {
            linExpr.addTerm(term.getCoefficient(), (GRBVar) term.getVar().getModelVar());
          } else {
            throw new LPModelException("Model variable is either null or is not an instance of GRBVar");
          }
        }
      }
    } else {
      throw new LPModelException("Expression is empty");
    }
    return linExpr;
  }
}
