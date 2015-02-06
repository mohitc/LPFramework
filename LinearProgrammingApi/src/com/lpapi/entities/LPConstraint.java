package com.lpapi.entities;

import com.lpapi.exception.LPConstraintException;
import com.lpapi.exception.LPModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LPConstraint<Z> {

  protected static final Logger log = LoggerFactory.getLogger(LPConstraint.class);

  private String identifier;

  private LPModel model;

  private LPExpression lhs, rhs;

  private LPOperator operator;

  public LPConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    if (model==null) {
      throw new LPConstraintException("Model cannot be null");
    }
    if (lhs == null) {
      throw new LPConstraintException("LHS Cannot be null");
    }
    if (rhs==null) {
      throw new LPConstraintException("RHS cannot be null");
    }
    if (operator==null) {
      throw new LPConstraintException("Operator cannot be null");
    }
    if (identifier==null) {
      throw new LPConstraintException("Identifier cannot be null");
    }
    this.identifier = identifier;
    this.model = model;
    this.lhs = lhs;
    this.rhs = rhs;
    this.operator = operator;
  }


  public LPExpression getLhs() {
    return lhs;
  }

  public LPExpression getRhs() {
    return rhs;
  }

  public LPOperator getOperator() {
    return operator;
  }

  protected abstract Z getModelConstraint();

  protected abstract void initModelConstraint() throws LPModelException;

  public LPModel getModel() {
    return model;
  }

  public String getIdentifier() {
    return identifier;
  }

}
