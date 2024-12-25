package com.lpapi.export.factory;

import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPExportException;
import com.lpapi.export.LPModelExporter;
import com.lpapi.export.LPModelExporterType;
import com.lpapi.export.jsonfile.JSONFileLPModelExporter;

public class LPModelExporterFactory {

  public static LPModelExporter instance(LPModel model, LPModelExporterType exporterType) throws LPExportException {
    if (exporterType==null)
      throw new LPExportException("Exporter type cannot be null");
    switch (exporterType) {
      case JSON_FILE: return new JSONFileLPModelExporter(model);
    }
    throw new LPExportException("Exporter not found for type: " + exporterType);
  }

}
