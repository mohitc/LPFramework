package com.lpapi.export.jsonfile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVarGroup;
import com.lpapi.exception.LPExportException;
import com.lpapi.exception.LPModelException;
import com.lpapi.export.LPVarGroupExporter;
import com.lpapi.export.jsonfile.exportdto.LPVarGroupDTO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class JSONFileVarGroupExporter extends LPVarGroupExporter {

  private String folderPath;

  public JSONFileVarGroupExporter(String folderPath, LPModel model, LPVarGroup group) throws LPExportException {
    super(model, group);
    this.folderPath = folderPath;
  }

  @Override
  public void export() throws LPExportException {
    try {
      //overwrite file if exists
      LPVarGroupDTO varGroupDTO = new LPVarGroupDTO(getModel(), getGroup());
      BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath + getModel().getIdentifier() +
          JSONFileConstants.VAR_GROUP_SUFFIX + getGroup().getIdentifier() + JSONFileConstants.MODEL_EXTENSION, false));
      //Write model parameters to map
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(writer, varGroupDTO);
      writer.close();
    } catch (IOException e) {
      log.error("Error while exporting file", e);
      throw new LPExportException("Error while exporting file: " + e.getMessage());
    } catch (LPModelException e) {
      log.error("Error while generating var group DTO", e);
      throw new LPExportException("Error while generating var group DTO: " + e.getMessage());
    }


  }
}
