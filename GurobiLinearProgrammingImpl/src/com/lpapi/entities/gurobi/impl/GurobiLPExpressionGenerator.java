package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.LPExpression;
import com.lpapi.entities.LPExpressionGenerator;
import com.lpapi.entities.LPExpressionTerm;
import com.lpapi.exception.LPModelException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;

import java.util.List;

public class GurobiLPExpressionGenerator implements LPExpressionGenerator<GRBLinExpr> {

  @Override
  public GRBLinExpr generateExpression(LPExpression expr) throws LPModelException {
    GRBLinExpr linExpr = new GRBLinExpr();
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
    return linExpr;
  }
}
