package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPConstraint;
import com.lpapi.entities.LPConstraintGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPModelException;

import java.util.HashSet;
import java.util.Set;

public class LPConstraintGroupDTO extends LPGroupDTO {

  private Set<LPConstraintDTO> constraints;

  //Default constructor
  public LPConstraintGroupDTO(){
  }

  public LPConstraintGroupDTO(LPModel model, LPConstraintGroup group) throws LPModelException {
    super(model, group);

    Set<LPConstraint> modelConstraints = model.getLPConstraints(group.getIdentifier());
    constraints = new HashSet<>();

    for (LPConstraint constraint: modelConstraints) {
      constraints.add(new LPConstraintDTO(constraint));
    }
  }

  public Set<LPConstraintDTO> getConstraints() {
    return constraints;
  }

  public void setConstraints(Set<LPConstraintDTO> constraints) {
    this.constraints = constraints;
  }
}
