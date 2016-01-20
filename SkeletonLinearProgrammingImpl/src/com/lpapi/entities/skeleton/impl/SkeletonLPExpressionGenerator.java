package com.lpapi.entities.skeleton.impl;

import com.lp.skeleton.entities.SkeletonExpr;
import com.lpapi.entities.LPExpression;
import com.lpapi.entities.LPExpressionGenerator;
import com.lpapi.exception.LPModelException;

public class SkeletonLPExpressionGenerator implements LPExpressionGenerator<SkeletonExpr> {

  @Override
  public SkeletonExpr generateExpression(LPExpression expr) throws LPModelException {
    SkeletonExpr linExpr = new SkeletonExpr();
/*
    List<LPExpressionTerm> termList = expr.getTermList();
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
*/
    return linExpr;
  }
}
