package com.lpapi.export;

import com.lpapi.entities.LPConstraintGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPConstraintGroupException;
import com.lpapi.exception.LPImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LPConstraintGroupImporter {

  protected static final Logger log = LoggerFactory.getLogger(LPConstraintGroupImporter.class);

  private LPModel model;

  private LPConstraintGroup lpConstraintGroup;

  public LPConstraintGroupImporter(LPModel model, String identifier, String description) throws LPImportException {
    if (model==null)
      throw new LPImportException("Model cannot be null");
    if (identifier==null)
      throw new LPImportException("Identifier cannot be null");

    //check if LP constraints group by the identifier can be created
    try {
      lpConstraintGroup = model.createLPConstraintGroup(identifier, description);
      this.model = model;
    } catch (LPConstraintGroupException e) {
      log.error("Error while creating the LP Constraint group");
      throw new LPImportException(e.getMessage());
    }
  }

  public LPModel getModel() {
    return model;
  }

  public LPConstraintGroup getLpConstraintGroup() {
    return lpConstraintGroup;
  }

  public abstract void importGroup() throws LPImportException;
}
