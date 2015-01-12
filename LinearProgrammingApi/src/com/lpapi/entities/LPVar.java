package com.lpapi.entities;

public class LPVar {

  private String identifier;

  private LPVarType varType;

  private double lBound;

  private double uBound;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public LPVarType getVarType() {
    return varType;
  }

  public void setVarType(LPVarType varType) {
    this.varType = varType;
  }

  public double getlBound() {
    return lBound;
  }

  public void setlBound(double lBound) {
    this.lBound = lBound;
  }

  public double getuBound() {
    return uBound;
  }

  public void setuBound(double uBound) {
    this.uBound = uBound;
  }
}
