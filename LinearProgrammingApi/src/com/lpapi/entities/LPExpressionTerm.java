package com.lpapi.entities;

public class LPExpressionTerm {

  private LPConstant constantTerm;

  private double coefficient;

  private LPVar var;

  public LPExpressionTerm (double coefficient, LPVar var) {
    this.coefficient = coefficient;
    this.var = var;
  }

  public LPExpressionTerm(double coefficient) {
    this.coefficient = coefficient;
  }

  public LPExpressionTerm(LPConstant constantTerm) {
    this.constantTerm = constantTerm;
  }

  public LPExpressionTerm(LPConstant constantTerm, LPVar var) {
    this.constantTerm = constantTerm;
    this.var = var;
  }

  public String toString() {
    //if constant, return constant, or else return coefficient . variable
    return (isConstant())? "" + getCoefficient(): getCoefficient() + "." + var.getIdentifier();
  }

  public boolean isConstant() {
    return (var==null);
  }

  public LPVar getVar() {
    return var;
  }

  public double getCoefficient() {
    if (constantTerm==null)
      return coefficient;
    else
      return constantTerm.getValue();
  }

  protected void setCoefficient(double coefficient) {
    this.coefficient = coefficient;
  }

  public LPExpressionTerm createCopy() {
/*
    if (constantTerm!=null)
      return new LPExpressionTerm(constantTerm, var);
    else
*/
      return new LPExpressionTerm(getCoefficient(), var);
  }
}
