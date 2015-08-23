package com.lpapi.export;

import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPExportException;

public abstract class LPModelExporter {

  private LPModel model;

  public LPModel getModel() {
    return model;
  }

  public LPModelExporter(LPModel model) throws LPExportException {
    if (model==null)
      throw new LPExportException("Model cannot be null");
    this.model = model;
  }

  public abstract void export() throws LPExportException;
}
