package com.lpapi.export;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVarGroup;
import com.lpapi.exception.LPVarGroupException;
import com.lpapi.exception.LPExportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LPVarGroupExporter {
  protected static final Logger log = LoggerFactory.getLogger(LPVarGroupExporter.class);

  private LPModel model;

  private LPVarGroup group;

  public LPVarGroupExporter(LPModel model, LPVarGroup group) throws LPExportException {
    if (model==null)
      throw new LPExportException("Model cannot be null");
    if (group==null)
      throw new LPExportException("Variable group cannot be null");
    try {
      if (!model.getLPVarGroup(group.getIdentifier()).equals(group)) {
        throw new LPExportException("LP Variable Group does not match with corresponding group in model");
      }
    } catch (LPVarGroupException e) {
      log.error("Variable group not found in model: ", e);
      throw new LPExportException(e.getMessage());
    }

    log.debug("New LP Constraint Group Exporter Created: Model (" + model.getIdentifier() + ", constraint group: " + group.getIdentifier() + ")");
    this.model = model;
    this.group = group;
  }


  public LPModel getModel() {
    return model;
  }

  public LPVarGroup getGroup() {
    return group;
  }

  public abstract void export() throws LPExportException;

}
