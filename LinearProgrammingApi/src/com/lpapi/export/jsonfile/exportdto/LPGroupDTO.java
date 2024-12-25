package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVarGroup;
import com.lpapi.exception.LPModelException;

public abstract class LPGroupDTO {

  private String identifier;

  private String description;

  //default constructor
  public LPGroupDTO() {
  }

  public LPGroupDTO(LPModel model, LPGroup grp) throws LPModelException {
    //Generate DTO
    if (model == null)
      throw new LPModelException("Model cannot be null");
    if (grp == null)
      throw new LPModelException("group cannot be null");

    this.identifier = grp.getIdentifier();
    this.description = grp.getDescription();
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
