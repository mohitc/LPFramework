package com.lpapi.export;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVarGroup;
import com.lpapi.exception.LPImportException;
import com.lpapi.exception.LPVarGroupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LPVarGroupImporter {

  protected static final Logger log = LoggerFactory.getLogger(LPConstraintGroupImporter.class);

  private LPModel model;

  private LPVarGroup lpVarGroup;

  public LPVarGroupImporter(LPModel model, String identifier, String description) throws LPImportException {
    if (model==null)
      throw new LPImportException("Model cannot be null");
    if (identifier==null)
      throw new LPImportException("Identifier cannot be null");

    //check if LP constraints group by the identifier can be created
    try {
      lpVarGroup = model.createLPVarGroup(identifier, description);
      this.model = model;
    } catch (LPVarGroupException e) {
      log.error("Error while creating the LP Variable group");
      throw new LPImportException(e.getMessage());
    }
  }

  public LPModel getModel() {
    return model;
  }

  public LPVarGroup getLpVarGroup() {
    return lpVarGroup;
  }

  public abstract void importGroup() throws LPImportException;
}
