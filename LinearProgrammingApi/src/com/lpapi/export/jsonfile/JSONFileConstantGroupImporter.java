package com.lpapi.export.jsonfile;

import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPImportException;
import com.lpapi.export.LPConstantGroupImporter;

/**
 * Created by mohit on 8/25/15.
 */
public class JSONFileConstantGroupImporter extends LPConstantGroupImporter {

  private String folderPath;

  public JSONFileConstantGroupImporter(String folderPath, LPModel model, String identifier, String description) throws LPImportException {
    super(model, identifier, description);
    this.folderPath = folderPath;
  }

  @Override
  public void importGroup() throws LPImportException {

  }
}
