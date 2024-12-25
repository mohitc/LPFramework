package com.lpapi.entities;

import com.lpapi.entities.exception.LPConstraintException;

public class LPConstraint {
  private LPExpression lhs, rhs;

  private LPOperator operator;

  public LPConstraint(LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    if (lhs == null) {
      throw new LPConstraintException("LHS Cannot be null");
    }
    if (rhs==null)
      throw new LPConstraintException("RHS cannot be null");
    if (operator==null)
      throw new LPConstraintException("Operator cannot be null");
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
}
