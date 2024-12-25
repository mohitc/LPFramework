package com.lpapi.export.jsonfile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpapi.entities.LPConstantGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPExportException;
import com.lpapi.exception.LPModelException;
import com.lpapi.export.LPConstantGroupExporter;
import com.lpapi.export.jsonfile.exportdto.LPConstantGroupDTO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class JSONFileConstantGroupExporter extends LPConstantGroupExporter {

  private String folderPath;

  public JSONFileConstantGroupExporter(String folderPath, LPModel model, LPConstantGroup group) throws LPExportException {
    super(model, group);
    this.folderPath = folderPath;
  }

  @Override
  public void export() throws LPExportException {
    try {
      //overwrite file if exists
      LPConstantGroupDTO constantGroupDTO = new LPConstantGroupDTO(getModel(), getGroup());
      Path path = Paths.get(folderPath + getModel().getIdentifier() +
          JSONFileConstants.CONSTANT_GROUP_SUFFIX + getGroup().getIdentifier() + JSONFileConstants.MODEL_EXTENSION);
      BufferedWriter writer = Files.newBufferedWriter(path, JSONFileConstants.ENCODING);
      //Write model parameters to map
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(writer, constantGroupDTO);
      writer.close();
    } catch (IOException e) {
      log.error("Error while exporting file", e);
      throw new LPExportException("Error while exporting file: " + e.getMessage());
    } catch (LPModelException e) {
      log.error("Error while generating constant group DTO", e);
      throw new LPExportException("Error while generating model DTO: " + e.getMessage());
    }
  }
}
