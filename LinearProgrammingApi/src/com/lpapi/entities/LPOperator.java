package com.lpapi.entities;

public enum LPOperator {
  GREATER_EQUAL(">="),
  LESS_EQUAL("<="),
  EQUAL("=");

  String shortRep;

  LPOperator(String shortRep) {
    this.shortRep = shortRep;
  }

  public String getShortRepresentation() {
    return shortRep;
  }
}
