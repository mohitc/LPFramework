package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarType;

public class LPVarDTO {

  private String identifier;

  private LPVarType varType;

  private double lBound;

  private double uBound;

  private Double result;

  public LPVarDTO() {}

  public LPVarDTO(LPVar lpVar) {
    this.identifier = lpVar.getIdentifier();
    this.varType = lpVar.getVarType();
    this.lBound = lpVar.getlBound();
    this.uBound = lpVar.getuBound();
    if (lpVar.isResultSet())
      this.result = lpVar.getResult();
  }

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

  public Double getResult() {
    return result;
  }

  public void setResult(Double result) {
    this.result = result;
  }
}
