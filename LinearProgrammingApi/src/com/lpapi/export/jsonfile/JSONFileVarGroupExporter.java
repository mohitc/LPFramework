package com.lpapi.export.jsonfile;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVarGroup;
import com.lpapi.exception.LPExportException;
import com.lpapi.export.LPVarGroupExporter;

public class JSONFileVarGroupExporter extends LPVarGroupExporter {

  private String folderPath;

  public JSONFileVarGroupExporter(String folderPath, LPModel model, LPVarGroup group) throws LPExportException {
    super(model, group);
    this.folderPath = folderPath;
  }

  @Override
  public void export() throws LPExportException {

  }
}
