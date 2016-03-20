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

  private String groupID;

  public LPConstantGroupImporter(LPModel model, String identifier) throws LPImportException {
    if (model==null)
      throw new LPImportException("Model cannot be null");
    if (identifier==null)
      throw new LPImportException("Identifier cannot be null");

    //check if LP constant group already exists, and if not, then try to create the group
    this.model = model;
    this.groupID = identifier;
  }

  public LPModel getModel() {
    return model;
  }

  public abstract void importGroup() throws LPImportException;

  public String getGroupID() {
    return groupID;
  }

}
