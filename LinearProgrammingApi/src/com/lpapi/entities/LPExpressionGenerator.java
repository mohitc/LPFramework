package com.lpapi.entities;

import com.lpapi.exception.LPModelException;

public interface LPExpressionGenerator<T> {

  public T generateExpression(LPExpression expr) throws LPModelException;
}
