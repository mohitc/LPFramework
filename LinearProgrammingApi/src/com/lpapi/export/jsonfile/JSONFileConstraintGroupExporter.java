package com.lpapi.export.jsonfile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpapi.entities.LPConstraintGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPExportException;
import com.lpapi.exception.LPModelException;
import com.lpapi.export.LPConstraintGroupExporter;
import com.lpapi.export.jsonfile.exportdto.LPConstraintGroupDTO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JSONFileConstraintGroupExporter extends LPConstraintGroupExporter {

  private String folderPath;

  public JSONFileConstraintGroupExporter(String folderPath, LPModel model, LPConstraintGroup group) throws LPExportException {
    super(model, group);
    this.folderPath = folderPath;
  }

  @Override
  public void export() throws LPExportException {
    try {
      //overwrite file if exists
      LPConstraintGroupDTO constraintGroupDTO = new LPConstraintGroupDTO(getModel(), getGroup());
      Path path = Paths.get(folderPath + getModel().getIdentifier() +
          JSONFileConstants.CONSTR_GROUP_SUFFIX + getGroup().getIdentifier() + JSONFileConstants.MODEL_EXTENSION);
      BufferedWriter writer = Files.newBufferedWriter(path, JSONFileConstants.ENCODING);
      //Write model parameters to map
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(writer, constraintGroupDTO);
      writer.close();
    } catch (IOException e) {
      log.error("Error while exporting file", e);
      throw new LPExportException("Error while exporting file: " + e.getMessage());
    } catch (LPModelException e) {
      log.error("Error while generating constraint group DTO", e);
      throw new LPExportException("Error while generating model DTO: " + e.getMessage());
    }


  }
}
