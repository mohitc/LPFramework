package com.lpapi.export;

import com.lpapi.entities.LPConstantGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPConstantGroupException;
import com.lpapi.exception.LPImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LPConstantGroupImporter {

  protected static final Logger log = LoggerFactory.getLogger(LPConstantGroupImporter.class);

  private LPModel model;

  private LPConstantGroup lpConstantGroup;

  public LPConstantGroupImporter(LPModel model, String identifier, String description) throws LPImportException {
    if (model==null)
      throw new LPImportException("Model cannot be null");
    if (identifier==null)
      throw new LPImportException("Identifier cannot be null");

    //check if LP constraints group by the identifier can be created
    try {
      lpConstantGroup = model.createLPConstantGroup(identifier, description);
      this.model = model;
    } catch (LPConstantGroupException e) {
      log.error("Error while creating the LP Constraint group");
      throw new LPImportException(e.getMessage());
    }
  }

  public LPModel getModel() {
    return model;
  }

  public LPConstantGroup getLpConstantGroup() {
    return lpConstantGroup;
  }

  public abstract void importGroup() throws LPImportException;
}
