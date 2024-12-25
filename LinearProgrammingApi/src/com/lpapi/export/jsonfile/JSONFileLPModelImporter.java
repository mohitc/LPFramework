package com.lpapi.export.jsonfile;

import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPImportException;
import com.lpapi.export.LPModelImporter;

public class JSONFileLPModelImporter extends LPModelImporter {

  public JSONFileLPModelImporter(LPModel model) throws LPImportException {
    super(model);
  }

  @Override
  public void importModel() {

  }
}
