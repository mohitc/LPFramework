package com.lpapi.export.jsonfile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpapi.entities.LPConstraintGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.*;
import com.lpapi.export.LPConstraintGroupImporter;
import com.lpapi.export.jsonfile.exportdto.LPConstraintDTO;
import com.lpapi.export.jsonfile.exportdto.LPConstraintGroupDTO;

import java.io.File;
import java.io.IOException;

public class JSONFileConstraintGroupImporter extends LPConstraintGroupImporter {

  private String folderPath;

  public JSONFileConstraintGroupImporter(String folderPath, LPModel model, String identifier) throws LPImportException {
    super(model, identifier);
    this.folderPath = folderPath;
  }

  @Override
  public void importGroup() throws LPImportException {
    try {
      File file = new File(folderPath + getModel().getIdentifier() +
          JSONFileConstants.CONSTR_GROUP_SUFFIX + getGroupID() + JSONFileConstants.MODEL_EXTENSION);

      //Read model parameters to DTO
      ObjectMapper mapper = new ObjectMapper();
      LPConstraintGroupDTO groupDTO = mapper.readValue(file, LPConstraintGroupDTO.class);

      LPConstraintGroup group;

      //See if group already exists, and if not try and create the group
      if (getModel().getLPConstraintGroupIDs().contains(getGroupID())) {
        group = getModel().getLPConstraintGroup(getGroupID());
      } else {
        group = getModel().createLPConstraintGroup(groupDTO.getIdentifier(), groupDTO.getDescription());
      }

      for (LPConstraintDTO constraintDTO: groupDTO.getConstraints()) {
        getModel().addConstraint(constraintDTO.getIdentifier(), constraintDTO.getLhs().createExpression(getModel()),
            constraintDTO.getOperator(), constraintDTO.getRhs().createExpression(getModel()), group);
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
    } catch (LPConstraintGroupException | LPConstraintException | LPExpressionException | LPVarException e) {
      log.error("Error while generating the constant group", e);
      throw new LPImportException("Error while generating the constant group: " + e.getMessage());
    }

  }
}
