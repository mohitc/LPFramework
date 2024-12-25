package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPModelException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LPModelDTO {

  private String identifier;

  private Map<String, String> variableGroups;

  private Map<String, String> constraintGroups;

  private LPExpressionDTO objectiveFn;

  public LPModelDTO() {}

  public LPModelDTO(LPModel model) throws LPModelException {
    identifier = model.getIdentifier();
    variableGroups = new HashMap<>();
    Set<String> varGroupIDs = model.getLPVarGroupIDs();
    for (String varID : varGroupIDs) {
      variableGroups.put(varID, model.getLPVarGroup(varID).getDescription());
    }

    constraintGroups = new HashMap<>();
    Set<String> constrGroupIDs = model.getLPConstraintGroupIDs();
    for (String constrID : constrGroupIDs) {
      variableGroups.put(constrID, model.getLPConstraintGroup(constrID).getDescription());
    }

    if (model.getObjFn()!=null) {
      this.objectiveFn = new LPExpressionDTO(model.getObjFn());
    }
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public Map<String, String> getVariableGroups() {
    return variableGroups;
  }

  public void setVariableGroups(Map<String, String> variableGroups) {
    this.variableGroups = variableGroups;
  }

  public Map<String, String> getConstraintGroups() {
    return constraintGroups;
  }

  public void setConstraintGroups(Map<String, String> constraintGroups) {
    this.constraintGroups = constraintGroups;
  }

  public LPExpressionDTO getObjectiveFn() {
    return objectiveFn;
  }

  public void setObjectiveFn(LPExpressionDTO objectiveFn) {
    this.objectiveFn = objectiveFn;
  }
}
