package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarGroup;
import com.lpapi.exception.LPModelException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LPVarGroupDTO {

  private List<LPVarDTO> vars;

  public LPVarGroupDTO() {}

  public LPVarGroupDTO(LPModel model, LPVarGroup varGrp) throws LPModelException {
    //Generate DTO
    if (model==null)
      throw new LPModelException("Model cannot be null");
    if (varGrp==null)
      throw new LPModelException("Variable group cannot be null");

    Set<LPVar> lpVars = model.getLPVars(varGrp.getIdentifier());
    vars = new ArrayList<>();
    for (LPVar var: lpVars) {
      vars.add(new LPVarDTO(var));
    }
  }

  public List<LPVarDTO> getVars() {
    return vars;
  }

  public void setVars(List<LPVarDTO> vars) {
    this.vars = vars;
  }
}
