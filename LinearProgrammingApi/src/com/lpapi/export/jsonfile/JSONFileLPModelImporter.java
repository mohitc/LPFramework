package com.lpapi.export.jsonfile;

import com.lpapi.entities.LPExpression;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.*;
import com.lpapi.export.LPModelImporter;
import com.lpapi.export.jsonfile.exportdto.LPModelDTO;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class JSONFileLPModelImporter extends LPModelImporter {

  private String folderPath;

  private static final Logger log = LoggerFactory.getLogger(JSONFileLPModelImporter.class);

  public JSONFileLPModelImporter(LPModel model) throws LPImportException {
    super(model);
    this.setFolderPath(JSONFileConstants.DEFAULT_JSON_FOLDER_PATH);
  }

  @Override
  public void importModel() throws LPImportException {

    try {
      File file = new File(folderPath + getModel().getIdentifier() +
          JSONFileConstants.MODEL_EXTENSION);

      //Read model parameters to DTO
      ObjectMapper mapper = new ObjectMapper();
      LPModelDTO modelDTO = mapper.readValue(file, LPModelDTO.class);

      //Read constant groups from IDs
      for (String constantGroup: modelDTO.getConstantGroups()) {
        JSONFileConstantGroupImporter importer = new JSONFileConstantGroupImporter(getFolderPath(), getModel(), constantGroup);
        importer.importGroup();
      }

      //Read var groups from IDs
      for (String varGroup: modelDTO.getVariableGroups()) {
        JSONFileVarGroupImporter importer = new JSONFileVarGroupImporter(getFolderPath(), getModel(), varGroup);
        importer.importGroup();
      }

      //Read constraint groups from IDs
      for (String constraintGroup: modelDTO.getConstraintGroups()) {
        JSONFileConstraintGroupImporter importer = new JSONFileConstraintGroupImporter(getFolderPath(),
            getModel(), constraintGroup);
        importer.importGroup();
      }

      //Read Objective Function
      if (modelDTO.getObjectiveFn()!=null && modelDTO.getObjType()!=null) {
        LPExpression expression = modelDTO.getObjectiveFn().createExpression(getModel());
        getModel().setObjFn(expression, modelDTO.getObjType());
      }

      getModel().setModelSolutionParams(modelDTO.getSolnParams());

    } catch (JsonParseException e) {
      log.error("Error while parsing JSON", e);
      throw new LPImportException("Error while parsing JSON: " + e.getMessage());
    } catch (JsonMappingException e) {
      log.error("Error while mapping JSON to LP Model.", e);
      throw new LPImportException("Error while mapping JSON to LP Model: " + e.getMessage());
    } catch (IOException e) {
      log.error("I/O error while reading file.", e);
      throw new LPImportException("I/O error while reading file: " + e.getMessage());
    } catch (LPModelException e) {
      log.error("Error while generating the objective function", e);
      throw new LPImportException("Error while generating the objective function: " + e.getMessage());
    }
  }

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String path) throws LPImportException {
    checkFolderPath(path);
    this.folderPath = path;
  }

  private void checkFolderPath(String folderPath) throws LPImportException {
    if (folderPath==null) {
      throw new LPImportException("Folder Path cannot be null");
    }
    if (folderPath.length()==0)
      throw new LPImportException("Folder path cannot be an empty string");
    //Add trailing slash
    if (folderPath.charAt(folderPath.length()-1)!='/')
      folderPath = folderPath + "/";
    File file = new File(folderPath);
    if (!file.exists() || !file.isDirectory()) {
      throw new LPImportException("Path is not a valid folder path");
    }
  }
}
