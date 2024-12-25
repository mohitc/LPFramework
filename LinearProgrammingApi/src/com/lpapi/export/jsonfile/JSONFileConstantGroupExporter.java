package com.lpapi.export.jsonfile;

import com.lpapi.entities.LPConstantGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPExportException;
import com.lpapi.export.LPConstantGroupExporter;


public class JSONFileConstantGroupExporter extends LPConstantGroupExporter {

  private String folderPath;

  public JSONFileConstantGroupExporter(String folderPath, LPModel model, LPConstantGroup group) throws LPExportException {
    super(model, group);
    this.folderPath = folderPath;
  }

  @Override
  public void export() throws LPExportException {

  }
}
