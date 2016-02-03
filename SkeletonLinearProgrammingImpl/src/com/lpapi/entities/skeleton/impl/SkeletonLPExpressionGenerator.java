package com.lpapi.entities.skeleton.impl;

import com.lp.skeleton.entities.SkeletonExpr;
import com.lpapi.entities.LPExpression;
import com.lpapi.entities.LPExpressionGenerator;
import com.lpapi.entities.LPExpressionTerm;
import com.lpapi.exception.LPModelException;

import java.util.List;

public class SkeletonLPExpressionGenerator implements LPExpressionGenerator<SkeletonExpr> {

  private String LOG_PREFIX = null;

  public SkeletonLPExpressionGenerator() {
  }

  public SkeletonLPExpressionGenerator(String logPrefix) {
    if (logPrefix!=null) {
      this.LOG_PREFIX = logPrefix;
    }
  }
  @Override
  public SkeletonExpr generateExpression(LPExpression expr) throws LPModelException {
    SkeletonExpr linExpr = new SkeletonExpr();

    List<LPExpressionTerm> termList = expr.getTermList();
    if ((termList==null) || (termList.size()==0)){
      throw new LPModelException((LOG_PREFIX!=null ? LOG_PREFIX+ ": " : "") + "Expression is empty");
    }
    return linExpr;
  }
}
