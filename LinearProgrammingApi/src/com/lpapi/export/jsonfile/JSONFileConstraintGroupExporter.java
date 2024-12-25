package com.lpapi.export.jsonfile;

import com.lpapi.entities.LPConstraintGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPExportException;
import com.lpapi.export.LPConstraintGroupExporter;

public class JSONFileConstraintGroupExporter extends LPConstraintGroupExporter {

  private String folderPath;

  public JSONFileConstraintGroupExporter(String folderPath, LPModel model, LPConstraintGroup group) throws LPExportException {
    super(model, group);
    this.folderPath = folderPath;
  }

  @Override
  public void export() throws LPExportException {

  }
}
