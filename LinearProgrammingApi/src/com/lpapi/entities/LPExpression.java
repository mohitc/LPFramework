package com.lpapi.entities;

import com.lpapi.entities.exception.LPExpressionException;

import java.util.ArrayList;
import java.util.List;

public class LPExpression {

  private List<LPExpressionTerm> expressionList;

  public LPExpression() {
    this.expressionList = new ArrayList<>();
  }

  public void addTerm(LPExpressionTerm term) throws LPExpressionException {
    if (term==null)
      throw new LPExpressionException("Term cannot be null");
    expressionList.add(term);
  }

  public void addTerm (double constant) {
    expressionList.add(new LPExpressionTerm(constant));
  }

  public void addTerm (double constant, LPVar var) {
    expressionList.add(new LPExpressionTerm(constant, var));
  }

  public void add(LPExpression expression) {
    if (expression.expressionList!=null) {
      this.expressionList.addAll(expression.expressionList);
    }
  }

}
