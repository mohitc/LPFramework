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

  private String groupID;

  public LPConstraintGroupImporter(LPModel model, String identifier) throws LPImportException {
    if (model==null)
      throw new LPImportException("Model cannot be null");
    if (identifier==null)
      throw new LPImportException("Identifier cannot be null");

    this.groupID = identifier;
    this.model = model;
  }

  public LPModel getModel() {
    return model;
  }

  public abstract void importGroup() throws LPImportException;

  public String getGroupID() {
    return groupID;
  }
}
