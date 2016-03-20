package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPConstant;

public class LPConstantDTO {

  private String identifier;

  private double value;

  //Default Constructor
  public LPConstantDTO() {
  }

  public LPConstantDTO(LPConstant constant) {
    this.identifier = constant.getIdentifier();
    this.value = constant.getValue();
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }
}
