package com.lpapi.export;

import com.lpapi.entities.LPConstantGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPConstantGroupException;
import com.lpapi.exception.LPExportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LPConstantGroupExporter {

  protected static final Logger log = LoggerFactory.getLogger(LPConstantGroupExporter.class);

  private LPModel model;

  private LPConstantGroup group;

  public LPConstantGroupExporter(LPModel model, LPConstantGroup group) throws LPExportException {
    if (model==null)
      throw new LPExportException("Model cannot be null");
    if (group==null)
      throw new LPExportException("Constant group cannot be null");
    try {
      if (!model.getLPConstantGroup(group.getIdentifier()).equals(group)) {
        throw new LPExportException("LP Constraint Group does not match with corresponding group in model");
      }
    } catch (LPConstantGroupException e) {
      log.error("Constant group not found in model: ", e);
      throw new LPExportException(e.getMessage());
    }

    log.debug("New LP Constant Group Exporter Created: Model (" + model.getIdentifier() + ", constant group: " + group.getIdentifier() + ")");
    this.model = model;
    this.group = group;
  }


  public LPModel getModel() {
    return model;
  }

  public LPConstantGroup getGroup() {
    return group;
  }

  public abstract void export() throws LPExportException;
}
