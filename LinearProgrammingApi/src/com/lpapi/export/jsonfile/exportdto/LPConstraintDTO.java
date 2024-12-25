package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPConstraint;
import com.lpapi.entities.LPOperator;

public class LPConstraintDTO {

  private String identifier;

  private LPExpressionDTO lhs;

  private LPExpressionDTO rhs;

  private LPOperator operator;

  //Default constructor
  public LPConstraintDTO(){
  }

  public LPConstraintDTO(LPConstraint constraint) {
    this.operator = constraint.getOperator();
    this.identifier = constraint.getIdentifier();
    this.lhs = new LPExpressionDTO(constraint.getLhs());
    this.rhs = new LPExpressionDTO(constraint.getRhs());
  }


  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public LPExpressionDTO getLhs() {
    return lhs;
  }

  public void setLhs(LPExpressionDTO lhs) {
    this.lhs = lhs;
  }

  public LPExpressionDTO getRhs() {
    return rhs;
  }

  public void setRhs(LPExpressionDTO rhs) {
    this.rhs = rhs;
  }

  public LPOperator getOperator() {
    return operator;
  }

  public void setOperator(LPOperator operator) {
    this.operator = operator;
  }
}
