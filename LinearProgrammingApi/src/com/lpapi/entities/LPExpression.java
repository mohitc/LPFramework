package com.lpapi.entities;

import com.lpapi.exception.LPExpressionException;
import com.lpapi.exception.LPVarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LPExpression {

  private static final Logger log = LoggerFactory.getLogger(LPExpression.class);

  private List<LPExpressionTerm> expressionList;

  private LPModel model;

  public LPExpression(LPModel model) throws LPExpressionException {
    this.expressionList = new ArrayList<>();
    if (model!=null)
      this.model = model;
    else
      throw new LPExpressionException("Model cannot be null");
  }

  public void addTerm(LPExpressionTerm term) throws LPExpressionException {
    if (term==null)
      throw new LPExpressionException("Term cannot be null");
    try {
      if ((!term.isConstant() && model.getLPVar(term.getVar().getIdentifier()).equals(term.getVar())) || (term.isConstant()))
        expressionList.add(term);
    } catch (LPVarException e) {
      throw new LPExpressionException("Invalid variable used in term: " + term.getVar().getIdentifier());
    }
  }

  public void addTerm (double constant) {
    expressionList.add(new LPExpressionTerm(constant));
  }

  public void addTerm (LPConstant constant) {
    expressionList.add(new LPExpressionTerm(constant));
  }

  public void addTerm (LPVar var) {
    expressionList.add(new LPExpressionTerm(1.0, var));
  }

  public void addTerm (double constant, LPVar var) throws LPExpressionException {
    expressionList.add(new LPExpressionTerm(constant, var));
  }

  public void addTerm (LPConstant constant, LPVar var) {
    expressionList.add(new LPExpressionTerm(constant, var));
  }

  public LPExpression add(LPExpression expression) {
    if (expression.expressionList!=null) {
      this.expressionList.addAll(expression.expressionList);
    }
    return this;
  }

  public LPExpression multiply(double constant) {
    for (LPExpressionTerm term: this.expressionList) {
      term.setCoefficient(term.getCoefficient()*constant);
    }
    return this;
  }

  public LPExpression reduce() {
    if (expressionList.size()==0) {
      //no reduction required
      return this;
    }
    //reduce occurences of terms to single instances
    Map<String, LPExpressionTerm> reduceMap = new HashMap<>();
    LPExpressionTerm constant = new LPExpressionTerm(0, null);
    for (LPExpressionTerm term: expressionList) {
      if (term.isConstant()) {
        constant.setCoefficient(constant.getCoefficient() + term.getCoefficient());
      } else {
        LPExpressionTerm temp;
        if (reduceMap.containsKey(term.getVar().getIdentifier())) {
          temp = reduceMap.get(term.getVar().getIdentifier());
          temp.setCoefficient(temp.getCoefficient() + term.getCoefficient());
        } else {
          temp = new LPExpressionTerm(term.getCoefficient(), term.getVar());
          reduceMap.put(temp.getVar().getIdentifier(), temp);
        }
      }
    }
    List<LPExpressionTerm> reduceList = new ArrayList<>();
    if (constant.getCoefficient()!=0)
      reduceList.add(constant);
    reduceList.addAll(reduceMap.values());
    this.expressionList = reduceList;
    log.debug("LP Expression reduction complete");
    return this;
  }

  public Map<String, Double> getVarContribution() {
    Map<String, Double> varContributionMap = new HashMap<>();
    for (LPExpressionTerm term: expressionList) {
      if (!term.isConstant()) {
        String key = term.getVar().getIdentifier();
        if (varContributionMap.containsKey(key)) {
          double val = varContributionMap.get(key) + term.getCoefficient();
          varContributionMap.put(key, val);
        } else {
          varContributionMap.put(key, term.getCoefficient());
        }
      }
    }
    return varContributionMap;
  }

  public List<LPExpressionTerm> getTermList() {
    return Collections.unmodifiableList(expressionList);
  }

  public double getConstantContribution() {
    double contribution = 0;
    for (LPExpressionTerm term: expressionList) {
      if (term.isConstant()) {
        contribution = contribution + term.getCoefficient();
      }
    }
    return contribution;
  }

  public LPExpression createCopy () throws LPExpressionException {
    LPExpression copy = new LPExpression(model);
    List<LPExpressionTerm> list = getTermList();
    for (LPExpressionTerm term: list) {
      copy.addTerm(term.createCopy());
    }
    return copy;
  }
}
