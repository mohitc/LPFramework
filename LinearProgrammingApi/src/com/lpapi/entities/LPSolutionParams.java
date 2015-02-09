package com.lpapi.entities;


public enum LPSolutionParams {
  STATUS("Solution Status", LPSolutionStatus.class),
  OBJECTIVE("Objective Function Value", Double.class),
  MIP_GAP("MIP Gap", Double.class),
  TIME("Time in milliseconds", Long.class);

  private String description;

  private Class entityType;

  LPSolutionParams(String description, Class entityType){
    this.description = description;
    this.entityType = entityType;
  }

  public String getDescription(){
    return description;
  }

  public Class getEntityType() {
    return entityType;
  }
}
