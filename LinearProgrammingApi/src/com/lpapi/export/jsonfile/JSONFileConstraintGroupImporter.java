package com.lpapi.export.jsonfile;

import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPImportException;
import com.lpapi.export.LPConstraintGroupImporter;

public class JSONFileConstraintGroupImporter extends LPConstraintGroupImporter {

  private String folderPath;

  public JSONFileConstraintGroupImporter(String folderPath, LPModel model, String identifier, String description) throws LPImportException {
    super(model, identifier, description);
    this.folderPath = folderPath;
  }

  @Override
  public void importGroup() throws LPImportException {

  }
}
