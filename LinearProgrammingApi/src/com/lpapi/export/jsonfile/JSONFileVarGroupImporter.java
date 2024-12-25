package com.lpapi.export.jsonfile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarGroup;
import com.lpapi.exception.*;
import com.lpapi.export.LPVarGroupImporter;
import com.lpapi.export.jsonfile.exportdto.LPVarDTO;
import com.lpapi.export.jsonfile.exportdto.LPVarGroupDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JSONFileVarGroupImporter extends LPVarGroupImporter {

  private String folderPath;

  public JSONFileVarGroupImporter(String folderPath, LPModel model, String identifier) throws LPImportException {
    super(model, identifier);
    this.folderPath = folderPath;
  }

  @Override
  public void importGroup() throws LPImportException {
    try {
      Path path = Paths.get(folderPath + getModel().getIdentifier() +
          JSONFileConstants.VAR_GROUP_SUFFIX + getGroupID() + JSONFileConstants.MODEL_EXTENSION);

      //Read model parameters to DTO
      ObjectMapper mapper = new ObjectMapper();
      LPVarGroupDTO groupDTO = mapper.readValue(Files.newBufferedReader(path, JSONFileConstants.ENCODING),
          LPVarGroupDTO.class);

      LPVarGroup group;

      //See if group already exists, and if not try and create the group
      if (getModel().getLPVarGroupIDs().contains(getGroupID())) {
        group = getModel().getLPVarGroup(getGroupID());
      } else {
        group = getModel().createLPVarGroup(groupDTO.getIdentifier(), groupDTO.getDescription());
      }

      for (LPVarDTO varDTO: groupDTO.getVars()) {
        LPVar var = getModel().createLPVar(varDTO.getIdentifier(), varDTO.getVarType(), varDTO.getlBound(), varDTO.getuBound(), group);
        if (varDTO.getResult()!=null) {
          var.setResult(varDTO.getResult());
        }
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
    } catch (LPVarGroupException | LPVarException e) {
      log.error("Error while generating the variable group", e);
      throw new LPImportException("Error while generating the variable group: " + e.getMessage());
    }
  }
}
