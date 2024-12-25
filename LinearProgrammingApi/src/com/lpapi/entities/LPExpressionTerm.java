package com.lpapi.entities;

public class LPExpressionTerm {

  private double coefficient;

  private LPVar var;

  public LPExpressionTerm (double coefficient, LPVar var) {
    this.coefficient = coefficient;
    this.var = var;
  }

  public LPExpressionTerm(double coefficient) {
    this.coefficient = coefficient;
  }

  public String toString() {
    //if constant, return constant, or else return coefficient . variable
    return (isConstant())? "" + coefficient: coefficient + "." + var.getIdentifier();
  }

  public boolean isConstant() {
    return (var==null);
  }
}
