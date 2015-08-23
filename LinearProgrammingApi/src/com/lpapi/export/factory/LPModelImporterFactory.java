package com.lpapi.export.factory;

import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPImportException;
import com.lpapi.export.LPModelImporter;
import com.lpapi.export.LPModelImporterType;
import com.lpapi.export.jsonfile.JSONFileLPModelImporter;

public class LPModelImporterFactory {

  public static LPModelImporter instance(LPModel model, LPModelImporterType exporterType) throws LPImportException {
    if (exporterType==null)
      throw new LPImportException("Importer type cannot be null");
    switch (exporterType) {
      case JSON_FILE: return new JSONFileLPModelImporter(model);
    }
    throw new LPImportException("Importer not found for type: " + exporterType);
  }

}
