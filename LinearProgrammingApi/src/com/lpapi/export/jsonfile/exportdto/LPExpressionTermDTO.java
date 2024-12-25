package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPExpressionTerm;

public class LPExpressionTermDTO {

  private double coeff;

  private String varID;

  public LPExpressionTermDTO(){}

  public LPExpressionTermDTO(LPExpressionTerm term) {
    this.coeff = term.getCoefficient();
    this.varID = (term.getVar()!=null)?term.getVar().getIdentifier():null;
  }

  public double getCoeff() {
    return coeff;
  }

  public void setCoeff(double coeff) {
    this.coeff = coeff;
  }

  public String getVarID() {
    return varID;
  }

  public void setVarID(String varID) {
    this.varID = varID;
  }
}
