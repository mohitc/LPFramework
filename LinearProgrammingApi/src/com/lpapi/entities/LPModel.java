package com.lpapi.entities;

import com.lpapi.entities.exception.LPConstraintException;
import com.lpapi.entities.exception.LPModelException;
import com.lpapi.entities.exception.LPVarException;
import com.lpapi.entities.exception.LPVarGroupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LPModel {

  private static final Logger log = LoggerFactory.getLogger(LPModel.class);

  private static final String DEF_VAR_GROUP = "Default";

  private String identifier;

  private Map<String, LPVarGroup> lpVarGroup = new HashMap<>();

  private Map<String, LPVar> lpVarIdentifiers = new HashMap<>();

  private Map<LPVarGroup, Set<LPVar>> lpVars = new HashMap<>();

  private List<LPConstraint> lpConstraintList = new ArrayList<>();


  public LPModel(String identifier) throws LPModelException {
    createLPVarGroup(DEF_VAR_GROUP, "Default variable group used in the model");
    if (identifier==null) {
      this.identifier = "";
    } else {
      this.identifier = identifier;
    }
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
      throw new LPVarGroupException("Identifier (" + identifier +") not found in the model");
  }

  public Set<String> getLPVarGroupIDs() {
    return Collections.unmodifiableSet(lpVarGroup.keySet());
  }

  public LPVar createLPVar(String identifier, LPVarType type, double lBound, double uBound) throws LPVarException {
    try {
      return createLPVar(identifier, type, lBound, uBound, getLPVarGroup(DEF_VAR_GROUP));
    } catch (LPVarGroupException e) {
      log.error("Default var Group not created, exiting", e);
      System.exit(1);
      return null;
    }
  }

  public LPVar createLPVar(String identifier, LPVarType type, double lBound, double uBound, LPVarGroup group) throws LPVarException, LPVarGroupException {
    //check if var group is valid
    LPVarGroup used = getLPVarGroup(group.getIdentifier());
    LPVar var;
    synchronized (lpVarIdentifiers) {
      if (identifier==null)
        throw new LPVarException("Identifier cannot be null");
      if (lpVarIdentifiers.containsKey(identifier))
        throw new LPVarException("Variable with identifier (" + identifier + ") already exists");

      var = new LPVar(this, identifier, type);
      var.setBounds(lBound, uBound);
      //If no exception was throws, variable is valid, add to model
      lpVarIdentifiers.put(identifier, var);
      //add variable to set of corresponding var group
      Set<LPVar> vars = lpVars.get(used);
      vars.add(var);
      log.info("Variable created {}", this);
    }
    return var;
  }

  public LPVar getLPVar(String identifier) throws LPVarException {
    if (identifier==null)
      throw new LPVarException("Identifier cannot be null");
    if (lpVarIdentifiers.containsKey(identifier)) {
      return lpVarIdentifiers.get(identifier);
    } else {
      throw new LPVarException("Variable with identifier " + identifier + " not found in model");
    }
  }

  public void addConstraint(LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    LPConstraint constraint = new LPConstraint(lhs, operator, rhs);
    log.info("Constraint created successfully. Adding to model");
     lpConstraintList.add(constraint);
  }

  public List<LPConstraint> getConstraintList() {
    return Collections.unmodifiableList(lpConstraintList);
  }

  public String getIdentifier() {
    return identifier;
  }

}
