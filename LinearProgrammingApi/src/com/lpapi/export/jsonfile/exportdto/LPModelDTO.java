package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPObjType;
import com.lpapi.entities.LPSolutionParams;
import com.lpapi.exception.LPModelException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LPModelDTO {

  private String identifier;

  private Set<String> variableGroups;

  private Set<String> constraintGroups;

  private Set<String> constantGroups;

  private LPExpressionDTO objectiveFn;

  private LPObjType objType;

  private Map<LPSolutionParams, Object> solnParams;

  public LPModelDTO() {}

  public LPModelDTO(LPModel model) throws LPModelException {
    identifier = model.getIdentifier();
    variableGroups = model.getLPVarGroupIDs();
    constraintGroups = model.getLPConstraintGroupIDs();
    constantGroups = model.getLPConstantGroupIDs();

    if (model.getObjFn()!=null) {
      this.objectiveFn = new LPExpressionDTO(model.getObjFn());
    }

    if (model.getObjType()!=null) {
      this.objType = model.getObjType();
    }
    this.solnParams = model.getModelSolutionParams();
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }


  public LPExpressionDTO getObjectiveFn() {
    return objectiveFn;
  }

  public void setObjectiveFn(LPExpressionDTO objectiveFn) {
    this.objectiveFn = objectiveFn;
  }

  public Set<String> getConstraintGroups() {
    return constraintGroups;
  }

  public void setConstraintGroups(Set<String> constraintGroups) {
    this.constraintGroups = constraintGroups;
  }

  public Set<String> getConstantGroups() {
    return constantGroups;
  }

  public void setConstantGroups(Set<String> constantGroups) {
    this.constantGroups = constantGroups;
  }

  public Set<String> getVariableGroups() {
    return variableGroups;
  }

  public void setVariableGroups(Set<String> variableGroups) {
    this.variableGroups = variableGroups;
  }

  public LPObjType getObjType() {
    return objType;
  }

  public void setObjType(LPObjType objType) {
    this.objType = objType;
  }

  public Map<LPSolutionParams, Object> getSolnParams() {
    return solnParams;
  }

  public void setSolnParams(Map<LPSolutionParams, Object> solnParams) {
    this.solnParams = solnParams;
  }
}
