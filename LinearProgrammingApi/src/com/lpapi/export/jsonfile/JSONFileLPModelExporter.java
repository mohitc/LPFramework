package com.lpapi.export.jsonfile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpapi.entities.LPConstantGroup;
import com.lpapi.entities.LPConstraintGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVarGroup;
import com.lpapi.exception.LPExportException;
import com.lpapi.exception.LPModelException;
import com.lpapi.export.LPModelExporter;
import com.lpapi.export.jsonfile.exportdto.LPModelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class JSONFileLPModelExporter extends LPModelExporter {

  private static final Logger log = LoggerFactory.getLogger(JSONFileLPModelExporter.class);

  //Path to folder where model data is stored
  private String folderPath;

  public JSONFileLPModelExporter(LPModel model) throws LPExportException {
    super(model);
    this.setFolderPath(JSONFileConstants.DEFAULT_JSON_FOLDER_PATH);
  }

  private void checkFolderPath(String folderPath) throws LPExportException {
    if (folderPath==null) {
      throw new LPExportException("Folder Path cannot be null");
    }
    if (folderPath.length()==0)
      throw new LPExportException("Folder path cannot be an empty string");
    //Add trailing slash
    if (folderPath.charAt(folderPath.length()-1)!='/')
      folderPath = folderPath + "/";
    File file = new File(folderPath);
    if (!file.exists()) {
      log.info("Folder does not exist. Creating folder");
      boolean op = file.mkdirs();
      if (!op) {
        throw new LPExportException("Could not create the folder path : " + folderPath);
      }
    }
    if (!file.isDirectory()) {
      throw new LPExportException("Path is not a valid folder path");
    }
  }

  @Override
  public void export() throws LPExportException {
    try {
      //overwrite file if exists
      LPModelDTO modelDTO = new LPModelDTO(getModel());
      BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath + getModel().getIdentifier() +
          JSONFileConstants.MODEL_EXTENSION, false));
      //Write model parameters to map
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(writer, modelDTO);
      writer.close();
      //TODO Include calls to export variable groups and constraints
      log.info("Exporting variable groups");
      Set<String> varGroupIDs = getModel().getLPVarGroupIDs();
      for (String varGroupID : varGroupIDs) {
        LPVarGroup group = getModel().getLPVarGroup(varGroupID);
        JSONFileVarGroupExporter exporter = new JSONFileVarGroupExporter(getFolderPath(), getModel(), group);
        exporter.export();
      }
      log.info("Exporting constraint groups");
      Set<String> constrGroupIDs = getModel().getLPConstraintGroupIDs();
      for (String constrGroupID : constrGroupIDs) {
        LPConstraintGroup group = getModel().getLPConstraintGroup(constrGroupID);
        JSONFileConstraintGroupExporter exporter = new JSONFileConstraintGroupExporter(getFolderPath(), getModel(), group);
        exporter.export();
      }
      log.info("Exporting constant groups");
      Set<String> constantGroupIDs = getModel().getLPConstantGroupIDs();
      for (String constantGroupID : constantGroupIDs) {
        LPConstantGroup group = getModel().getLPConstantGroup(constantGroupID);
        JSONFileConstantGroupExporter exporter = new JSONFileConstantGroupExporter(getFolderPath(), getModel(), group);
        exporter.export();
      }
    } catch (IOException e) {
      log.error("Error while exporting file", e);
      throw new LPExportException("Error while exporting file: " + e.getMessage());
    } catch (LPModelException e) {
      log.error("Error while generating model DTO", e);
      throw new LPExportException("Error while generating model DTO: " + e.getMessage());
    }
  }

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String path) throws LPExportException {
    checkFolderPath(path);
    this.folderPath = path;
  }
}
