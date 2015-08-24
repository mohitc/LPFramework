package com.lpapi.entities;

import com.lpapi.exception.LPConstantException;

public class LPConstant {

  private String identifier;

  private double value;

  public LPConstant(String identifier, double value) throws LPConstantException {
    this.identifier = identifier;
    this.value = value;
  }

  public String getIdentifier() {
    return identifier;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public String toString() {
    return "[Constant] " + identifier + " : " + value;
  }
}
