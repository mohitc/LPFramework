package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPConstant;
import com.lpapi.entities.LPConstantGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPModelException;

import java.util.HashSet;
import java.util.Set;

public class LPConstantGroupDTO extends LPGroupDTO {

  private Set<LPConstantDTO> constants;

  //Default Constructor
  public LPConstantGroupDTO() {
  }

  public LPConstantGroupDTO (LPModel model, LPConstantGroup group) throws LPModelException {
    super(model, group);

    Set<LPConstant> modelConstants = model.getLPConstants(group.getIdentifier());
    constants = new HashSet<>();

    for (LPConstant constant: modelConstants) {
      constants.add(new LPConstantDTO(constant));
    }
  }

  public Set<LPConstantDTO> getConstants() {
    return constants;
  }

  public void setConstants(Set<LPConstantDTO> constants) {
    this.constants = constants;
  }
}
