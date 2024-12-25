package com.lpapi.entities;

import com.lpapi.entities.exception.LPModelException;
import com.lpapi.entities.exception.LPVarGroupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class LPModel {

  private static final Logger log = LoggerFactory.getLogger(LPModel.class);

  private static final String DEF_VAR_GROUP = "Default";

  private Map<String, LPVarGroup> lpVarGroup = new HashMap<>();

  public LPModel() throws LPModelException {
    createLPVarGroup(DEF_VAR_GROUP, "Default variable group used in the model");
  }

  public LPVarGroup createLPVarGroup(String identifier, String description) throws LPVarGroupException {
    if (identifier==null) {
      throw new LPVarGroupException("Identifier cannot be null");
    }
    if (lpVarGroup.containsKey(identifier)) {
      throw new LPVarGroupException("Identifier (" + identifier + ") already exists");
    }

    LPVarGroup group = new LPVarGroup(identifier, description);
    log.info("Created new LP Variable Group {}", group);
    lpVarGroup.put(identifier, group);
    return group;
  }

  public LPVarGroup getLPVarGroup(String identifier) throws LPVarGroupException {
    if (identifier==null) {
      throw new LPVarGroupException("Identifier cannot be null");
    }
    if (lpVarGroup.containsKey(identifier)) {
      return lpVarGroup.get(identifier);
    } else
      throw new LPVarGroupException("Identifier not found in the model");
  }

}
