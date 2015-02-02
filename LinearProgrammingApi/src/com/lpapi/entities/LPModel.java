package com.lpapi.entities;

import com.lpapi.entities.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * @param <X> Model Type
 * @param <Y> Model Variable Type
 * @param <Z> Model Constraint Type
 */
public abstract class LPModel <X, Y, Z> {

  protected static final Logger log = LoggerFactory.getLogger(LPModel.class);

  private static final String DEF_VAR_GROUP = "Default";

  private static final String DEF_CONSTR_GROUP = "Default_Constraints";

  private String identifier;

  private Map<String, LPVarGroup> lpVarGroup = new HashMap<>();

  private Map<String, LPVar> lpVarIdentifiers = new HashMap<>();

  private Map<LPVarGroup, Set<LPVar>> lpVars = new HashMap<>();

  private Map<String, LPConstraintGroup> lpConstraintGroup = new HashMap<>();

  private Map<String, LPConstraint> lpConstraintIdentifiers = new HashMap<>();

  private Map<LPConstraintGroup, Set<LPConstraint>> lpConstraints = new HashMap<>();

  private LPObjType objType;

  private LPExpression objFn;

  public LPModel(String identifier) throws LPModelException {
    createLPVarGroup(DEF_VAR_GROUP, "Default variable group used in the model");
    createLPConstraintGroup(DEF_CONSTR_GROUP, "Default constraint group used in the model");
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
    lpVars.put(group, new HashSet<LPVar>());
    return group;
  }

  public LPVarGroup getLPVarGroup(String identifier) throws LPVarGroupException {
    if (identifier==null) {
      throw new LPVarGroupException("Identifier cannot be null");
    }
    if (lpVarGroup.containsKey(identifier)) {
      return lpVarGroup.get(identifier);
    } else
      throw new LPVarGroupException("Variable Group Identifier (" + identifier +") not found in the model");
  }

  public Set<String> getLPVarGroupIDs() {
    return Collections.unmodifiableSet(lpVarGroup.keySet());
  }

  public LPVar createLPVar(String identifier, LPVarType type, double lBound, double uBound) throws LPVarException {
    try {
      return createLPVar(identifier, type, lBound, uBound, getLPVarGroup(DEF_VAR_GROUP));
    } catch (LPVarGroupException e) {
      log.error("Default Variable Group not created, exiting", e);
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

      var = getLPVarFactory().generateLPVar(this, identifier, type, lBound, uBound);
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

  public LPConstraintGroup createLPConstraintGroup(String identifier, String description) throws LPConstraintGroupException {
    if (identifier==null) {
      throw new LPConstraintGroupException("Identifier cannot be null");
    }
    if (lpConstraintGroup.containsKey(identifier)) {
      throw new LPConstraintGroupException("Identifier (" + identifier + ") already exists");
    }

    LPConstraintGroup group = new LPConstraintGroup(identifier, description);
    log.info("Created new LP Constraint Group {}", group);
    lpConstraintGroup.put(identifier, group);
    lpConstraints.put(group, new HashSet<LPConstraint>());
    return group;

  }

  public LPConstraintGroup getLPConstraintGroup(String identifier) throws LPConstraintGroupException {
    if (identifier==null) {
      throw new LPConstraintGroupException("Identifier cannot be null");
    }
    if (lpConstraintGroup.containsKey(identifier)) {
      return lpConstraintGroup.get(identifier);
    } else
      throw new LPConstraintGroupException("Constraint Group Identifier (" + identifier +") not found in the model");
  }

  public Set<String> getLPConstraintGroupIDs(){
    return Collections.unmodifiableSet(lpConstraintGroup.keySet());
  }

  public void addConstraint(String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    try {
      addConstraint(identifier, lhs, operator, rhs, getLPConstraintGroup(DEF_CONSTR_GROUP));
    } catch (LPConstraintGroupException e) {
      log.error("Default Constraint Group not created, exiting", e);
      System.exit(1);
    }
  }

  public void addConstraint(String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs, LPConstraintGroup group) throws LPConstraintException, LPConstraintGroupException {
    //check if var group is valid
    LPConstraintGroup used = getLPConstraintGroup(group.getIdentifier());
    synchronized (lpVarIdentifiers) {
      if (identifier==null)
        throw new LPConstraintException("Identifier cannot be null");
      if (lpConstraintIdentifiers.containsKey(identifier))
        throw new LPConstraintException("Constraint with identifier (" + identifier + ") already exists");

      LPConstraint constraint = getLPConstraintFactory().generateConstraint(this, identifier, lhs, operator, rhs);

      //If no exception was throws, variable is valid, add to model
      lpConstraintIdentifiers.put(identifier, constraint);
      //add variable to set of corresponding var group
      Set<LPConstraint> constraints = lpConstraints.get(used);
      constraints.add(constraint);
      log.info("Constraint created {}", this);
    }
  }

  public Collection<LPConstraint> getConstraintList() {
    return Collections.unmodifiableCollection(lpConstraintIdentifiers.values());
  }

  public String getIdentifier() {
    return identifier;
  }

  //method to get the model for the ILP
  public abstract X getModel();

  //Factory to generate the variables
  protected abstract LPVarFactory<Y> getLPVarFactory();

  //Factory to generate the constraints
  protected abstract LPConstraintFactory<Z> getLPConstraintFactory();


  //Method to initialize the model
  public abstract void initModel() throws LPModelException;

  //Method to initialize the variables
  public void initVars() throws LPModelException {
    log.info("Initializing model vars");
    for (LPVar var: lpVarIdentifiers.values()) {
      log.debug("Initializing variable: " + var.getIdentifier());
      var.initModelVar();
    }
    log.info("Model Variables initialized");
  }

  //Method to initialize the objective function
  public abstract void initObjectiveFunction() throws LPModelException;

  //method to initialize the constraints
  public void initConstraints() throws LPModelException {
    log.info("Initializing model constraints");
    for (LPConstraint constraint: lpConstraintIdentifiers.values()) {
      log.debug("Initializing constraint: " + constraint.getIdentifier());
      constraint.initModelConstraint();
    }
    log.info("Model constraints initialized");
  }

  //method to initialize the computation
  public abstract void computeModel() throws LPModelException;

  public LPExpression getObjFn() throws LPModelException {
    if (objFn==null)
      throw new LPModelException("Objective function has not been defined");
    return objFn;
  }

  public void setObjFn(LPExpression objFn, LPObjType type) throws LPModelException {
    if (objFn==null)
      throw new LPModelException("Objective function cannot be null");
    if (type == null)
      throw new LPModelException("Objective should be either to maximize or minimize");
    this.objType = type;
    this.objFn = objFn;

  }

  public LPObjType getObjType() {
    return objType;
  }
}
