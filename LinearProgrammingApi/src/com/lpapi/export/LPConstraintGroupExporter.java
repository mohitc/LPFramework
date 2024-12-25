package com.lpapi.export;

import com.lpapi.entities.LPConstraintGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPConstraintGroupException;
import com.lpapi.exception.LPExportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LPConstraintGroupExporter {

  protected static final Logger log = LoggerFactory.getLogger(LPConstraintGroupExporter.class);

  private LPModel model;

  private LPConstraintGroup group;

  public LPConstraintGroupExporter(LPModel model, LPConstraintGroup group) throws LPExportException {
    if (model==null)
      throw new LPExportException("Model cannot be null");
    if (group==null)
      throw new LPExportException("Constraint group cannot be null");
    try {
      if (!model.getLPConstraintGroup(group.getIdentifier()).equals(group)) {
        throw new LPExportException("LP Constraint Group does not match with corresponding group in model");
      }
    } catch (LPConstraintGroupException e) {
      log.error("Constraint group not found in model: ", e);
      throw new LPExportException(e.getMessage());
    }

    log.debug("New LP Constraint Group Exporter Created: Model (" + model.getIdentifier() + ", constraint group: " + group.getIdentifier() + ")");
    this.model = model;
    this.group = group;
  }


  public LPModel getModel() {
    return model;
  }

  public LPConstraintGroup getGroup() {
    return group;
  }

  public abstract void export() throws LPExportException;
}
