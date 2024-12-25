package com.lpapi.entities;

public enum LPVarType {
  DOUBLE ("Double variable"),
  INTEGER ("Integer variable"),
  BOOLEAN ("Boolean variable");

  private String description;

  LPVarType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
