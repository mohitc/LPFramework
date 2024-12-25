package com.lpapi.export.jsonfile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpapi.entities.LPConstantGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPConstantException;
import com.lpapi.exception.LPConstantGroupException;
import com.lpapi.exception.LPImportException;
import com.lpapi.export.LPConstantGroupImporter;
import com.lpapi.export.jsonfile.exportdto.LPConstantDTO;
import com.lpapi.export.jsonfile.exportdto.LPConstantGroupDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JSONFileConstantGroupImporter extends LPConstantGroupImporter {

  private String folderPath;

  public JSONFileConstantGroupImporter(String folderPath, LPModel model, String identifier) throws LPImportException {
    super(model, identifier);
    this.folderPath = folderPath;
  }

  @Override
  public void importGroup() throws LPImportException {
    try {
      Path path = Paths.get(folderPath + getModel().getIdentifier() +
          JSONFileConstants.CONSTANT_GROUP_SUFFIX + getGroupID() + JSONFileConstants.MODEL_EXTENSION);

      //Read model parameters to DTO
      ObjectMapper mapper = new ObjectMapper();
      LPConstantGroupDTO groupDTO = mapper.readValue(Files.newBufferedReader(path, JSONFileConstants.ENCODING),
          LPConstantGroupDTO.class);

      LPConstantGroup group;

      //See if group already exists, and if not try and create the group
      if (getModel().getLPConstantGroupIDs().contains(getGroupID())) {
        group = getModel().getLPConstantGroup(getGroupID());
      } else {
        group = getModel().createLPConstantGroup(groupDTO.getIdentifier(), groupDTO.getDescription());
      }

      for (LPConstantDTO constantDTO: groupDTO.getConstants()) {
        getModel().createLpConstant(constantDTO.getIdentifier(), constantDTO.getValue(), group);
      }

    } catch (JsonParseException e) {
      log.error("Error while parsing JSON", e);
      throw new LPImportException("Error while parsing JSON: " + e.getMessage());
    } catch (JsonMappingException e) {
      log.error("Error while mapping JSON to LP Model.", e);
      throw new LPImportException("Error while mapping JSON to LP Model: " + e.getMessage());
    } catch (IOException e) {
      log.error("I/O error while reading file.", e);
      throw new LPImportException("I/O error while reading file: " + e.getMessage());
    } catch (LPConstantGroupException | LPConstantException e) {
      log.error("Error while generating the constraint group", e);
      throw new LPImportException("Error while generating the constraint group: " + e.getMessage());
    }

  }
}
