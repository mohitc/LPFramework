package com.lpapi.entities.cplex.impl;

import com.lpapi.entities.LPExpression;
import com.lpapi.entities.LPExpressionGenerator;
import com.lpapi.entities.LPExpressionTerm;
import com.lpapi.exception.LPModelException;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CplexLPExpressionGenerator implements LPExpressionGenerator<IloNumExpr> {

  private static final Logger log = LoggerFactory.getLogger(CplexLPExpressionGenerator.class);

  private IloCplex model;

  public CplexLPExpressionGenerator(IloCplex model) throws LPModelException{
    if (model==null) {
      log.error("Cplex model cannot be null when instantiating expression generator");
      throw new LPModelException("Cplex model cannot be null when instantiating expression generator");
    }
    this.model = model;
  }

  @Override
  public IloNumExpr generateExpression(LPExpression expression) throws LPModelException {
    try {
      IloLinearNumExpr expr = model.linearNumExpr();
      List<LPExpressionTerm> termList = expression.getTermList();
      if ((termList != null) && (termList.size() != 0)) {
        for (LPExpressionTerm term : termList) {
          if (term.isConstant()) {
            expr.setConstant(expr.getConstant() + term.getCoefficient());
          } else {
            if ((term.getVar().getModelVar() != null) && (IloNumVar.class.isAssignableFrom(term.getVar().getModelVar().getClass()))) {
              expr.addTerm(term.getCoefficient(), (IloNumVar) term.getVar().getModelVar());
            } else {
              throw new LPModelException("Model variable is either null or is not an instance of IloNumVar");
            }
          }
        }
      } else {
        throw new LPModelException("Expression is empty");
      }
      return expr;
    }catch (IloException e) {
      log.error("Exception while generating Cplex expression term", e);
      throw new LPModelException("Exception while generating Cplex expression term" + e.getMessage());
    }
  }
}
