package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarGroup;
import com.lpapi.exception.LPModelException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LPVarGroupDTO extends LPGroupDTO {

  private List<LPVarDTO> vars;

  public LPVarGroupDTO() {}

  public LPVarGroupDTO(LPModel model, LPVarGroup varGrp) throws LPModelException {
    super(model, varGrp);

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
