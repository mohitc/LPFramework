package com.lpapi.entities;

import com.lpapi.entities.group.LPGroupInitializer;
import com.lpapi.entities.group.LPNameGenerator;
import com.lpapi.exception.*;
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

  private static final String DEF_CONST_GROUP = "Default_Constants";

  private String identifier;

  private Map<String, LPVarGroup> lpVarGroup = new HashMap<>();

  private Map<String, LPVar> lpVarIdentifiers = new HashMap<>();

  private Map<LPVarGroup, Set<LPVar>> lpVars = new HashMap<>();

  private Map<String, LPConstraintGroup> lpConstraintGroup = new HashMap<>();

  private Map<String, LPConstraint> lpConstraintIdentifiers = new HashMap<>();

  private Map<LPConstraintGroup, Set<LPConstraint>> lpConstraints = new HashMap<>();

  private Map<String, LPConstantGroup> lpConstantGroup = new HashMap<>();

  private Map<String, LPConstant> lpConstantIdentifiers = new HashMap<>();

  private Map<LPConstantGroup, Set<LPConstant>> lpConstants = new HashMap<>();

  private LPObjFnGenerator objFunGenerator = null;

  private LPObjType objType;

  private LPExpression objFn;

  private LPSolutionStatus solutionStatus;

  protected Map<LPSolutionParams, Object> solnParams = new HashMap<>();

  public LPModel(String identifier) throws LPModelException {
    createLPVarGroup(DEF_VAR_GROUP, "Default variable group used in the model");
    createLPConstraintGroup(DEF_CONSTR_GROUP, "Default constraint group used in the model");
    createLPConstantGroup(DEF_CONST_GROUP, "Default constant group used in the model");
    if (identifier==null) {
      this.identifier = "";
    } else {
      this.identifier = identifier;
    }
  }

  public LPConstantGroup createLPConstantGroup(String identifier, String description) throws LPConstantGroupException {
    return createLPConstantGroup(identifier, description, null, null);
  }

  public LPConstantGroup createLPConstantGroup(String identifier, String description, LPNameGenerator<?> generator, LPGroupInitializer initializer) throws LPConstantGroupException {
    if (identifier==null) {
      throw new LPConstantGroupException("Identifier cannot be null");
    }
    if (lpConstantGroup.containsKey(identifier)) {
      throw new LPConstantGroupException("Identifier (" + identifier + ") already exists");
    }

    LPConstantGroup group = new LPConstantGroup(this, identifier, description, generator, initializer);
    log.info("Created new LP Constant Group {}", group.toString());
    lpConstantGroup.put(identifier, group);
    lpConstants.put(group, new HashSet<>());
    return group;
  }

  public LPConstantGroup getLPConstantGroup(String identifier) throws LPConstantGroupException {
    if (identifier==null) {
      throw new LPConstantGroupException("Identifier cannot be null");
    }
    if (lpConstantGroup.containsKey(identifier)) {
      return lpConstantGroup.get(identifier);
    } else
      throw new LPConstantGroupException("Constant Group Identifier (" + identifier +") not found in the model");
  }

  public Set<String> getLPConstantGroupIDs() {
    return Collections.unmodifiableSet(lpConstantGroup.keySet());
  }

  public LPConstant createLpConstant(String identifier, double value) throws LPConstantException {
    try {
      return createLpConstant(identifier, value, getLPConstantGroup(DEF_CONST_GROUP));
    } catch (LPConstantGroupException e) {
      log.error("Default Constant Group not created, exiting", e);
      throw new RuntimeException("Default Constant Group not created, exiting");
    }
  }

  public LPConstant createLpConstant(String identifier, double value, LPConstantGroup group) throws LPConstantException, LPConstantGroupException {
    //check if var group is valid
    LPConstantGroup used = getLPConstantGroup(group.getIdentifier());
    LPConstant constant;
    synchronized (lpConstantIdentifiers) {
      if (identifier==null)
        throw new LPConstantException("Identifier cannot be null");
      if (lpConstantIdentifiers.containsKey(identifier))
        throw new LPConstantException("Constant with identifier (" + identifier + ") already exists");

      constant = new LPConstant(identifier, value);
      //If no exception was throws, constant is valid, add to model
      lpConstantIdentifiers.put(identifier, constant);
      //add variable to set of corresponding constant group
      Set<LPConstant> constants = lpConstants.get(used);
      constants.add(constant);
      log.debug("LPConstant created {}", constant.toString());
    }
    return constant;
  }

  public LPConstant getLPConstant(String identifier) throws LPConstantException {
    if (identifier==null)
      throw new LPConstantException("Identifier cannot be null");
    if (lpConstantIdentifiers.containsKey(identifier)) {
      return lpConstantIdentifiers.get(identifier);
    } else {
      throw new LPConstantException("Constant with identifier " + identifier + " not found in model");
    }
  }

  public Set<LPConstant> getLPConstants(String constGrpIdentifier) throws LPConstantGroupException {
    //check if constant Group exists
    LPConstantGroup localGrp = getLPConstantGroup(constGrpIdentifier);
    return Collections.unmodifiableSet(lpConstants.get(localGrp));
  }

  public LPVarGroup createLPVarGroup(String identifier, String description) throws LPVarGroupException {
    return createLPVarGroup(identifier, description, null, null);
  }

  public LPVarGroup createLPVarGroup(String identifier, String description, LPNameGenerator<?> generator, LPGroupInitializer initializer) throws LPVarGroupException {
    if (identifier==null) {
      throw new LPVarGroupException("Identifier cannot be null");
    }
    if (lpVarGroup.containsKey(identifier)) {
      throw new LPVarGroupException("Identifier (" + identifier + ") already exists");
    }

    LPVarGroup group = new LPVarGroup(this, identifier, description, generator, initializer);
    log.info("Created new LP Variable Group {}", group.toString());
    lpVarGroup.put(identifier, group);
    lpVars.put(group, new HashSet<>());
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
      throw new RuntimeException("Default Constant Group not created, exiting");
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
      log.debug("LPVariable created {}", var.toString());
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

  public Set<LPVar> getLPVars(String varGrpIdentifier) throws LPVarGroupException {
    //check if var Group exists
    LPVarGroup localGrp = getLPVarGroup(varGrpIdentifier);
    return Collections.unmodifiableSet(lpVars.get(localGrp));
  }

  public Set<String> getLPVarIdentifiers() {
    return Collections.unmodifiableSet(lpVarIdentifiers.keySet());
  }

  public LPConstraintGroup createLPConstraintGroup(String identifier, String description) throws LPConstraintGroupException {
    return createLPConstraintGroup(identifier, description, null, null);
  }

  public LPConstraintGroup createLPConstraintGroup(String identifier, String description, LPNameGenerator<?> generator, LPGroupInitializer initializer) throws LPConstraintGroupException {
    if (identifier==null) {
      throw new LPConstraintGroupException("Identifier cannot be null");
    }
    if (lpConstraintGroup.containsKey(identifier)) {
      throw new LPConstraintGroupException("Identifier (" + identifier + ") already exists");
    }

    LPConstraintGroup group = new LPConstraintGroup(this, identifier, description, generator, initializer);
    log.info("Created new LP Constraint Group {}", group.toString());
    lpConstraintGroup.put(identifier, group);
    lpConstraints.put(group, new HashSet<>());
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
      throw new RuntimeException("Default Constant Group not created, exiting");
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
      log.debug("LP Constraint created {}", constraint.toString());
    }
  }

  public Collection<LPConstraint> getConstraintList() {
    return Collections.unmodifiableCollection(lpConstraintIdentifiers.values());
  }

  public Set<LPConstraint> getLPConstraints(String constrGrpIdentifier) throws LPConstraintGroupException {
    //check if constraint Group exists
    LPConstraintGroup localGrp = getLPConstraintGroup(constrGrpIdentifier);
    return Collections.unmodifiableSet(lpConstraints.get(localGrp));
  }


  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }
  //method to get the model for the ILP
  public abstract X getModel();

  //Factory to generate the variables
  protected abstract LPVarFactory<Y> getLPVarFactory();

  //Factory to generate the constraints
  protected abstract LPConstraintFactory<Z> getLPConstraintFactory();


  //Method to initialize the model
  public abstract void initModel() throws LPModelException;

  //method to invoke the initialization of all the var groups
  public void initConstantGroups() throws LPModelException {
    log.info("Initializing Constant groups");
    for (LPConstantGroup group: lpConstantGroup.values()) {
      group.init();
    }
  }


  //method to invoke the initialization of all the var groups
  public void initVarGroups() throws LPModelException {
    log.info("Initializing Variable groups");
    for (LPVarGroup group: lpVarGroup.values()) {
      group.init();
    }
  }

  //Method to initialize the variables
  public void initVars() throws LPModelException {
    log.info("Initializing model vars");
    for (LPVar var: lpVarIdentifiers.values()) {
      log.debug("Initializing variable: " + var.getIdentifier());
      var.initModelVar();
    }
    log.info("Model Variables initialized");
  }

  //method to register an objective function generator
  public void attachObjectiveFunctionGenerator(LPObjFnGenerator generator) {
    if (generator!=null) {
      generator.setModel(this);
      this.objFunGenerator = generator;
      log.info("Objective function generator " + generator.getClass().getSimpleName() + " registered successfully");
    } else {
      log.error("Objective function generator cannot be null");
    }
  }

  //method to create the objective function from the generator
  public void initObjFunctionGenerator() throws LPModelException {
    if (objFunGenerator!=null) {
      log.info("Found Objective function generator. Using function to generate the objective function expression");
      this.setObjFn(objFunGenerator.generate(), objFunGenerator.getObjType());
    }
  }

  //Method to initialize the objective function
  public abstract void initObjectiveFunction() throws LPModelException;

  //method to invoke the initialization of all constraint groups
  public void initConstraintGroups() throws LPModelException {
    log.info("Initializing Constraint Groups");
    for (LPConstraintGroup group: lpConstraintGroup.values())
      group.init();
  }

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

  //method to extract result values to variables
  public abstract void extractResults() throws LPModelException;

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

  public Map<LPSolutionParams, Object> getModelSolutionParams() {
    return Collections.unmodifiableMap(solnParams);
  }

  //method only to be used when importing results
  public void setModelSolutionParams(Map<LPSolutionParams, Object> solnParams) {
    if (solnParams!=null) {
      this.solnParams = new HashMap<>(solnParams);
    }
  }

  public LPSolutionStatus getSolutionStatus() throws LPModelException {
    return getSolutionParam(LPSolutionParams.STATUS, LPSolutionStatus.class);
  }

  public long getComputationTime() throws LPModelException {
    return getSolutionParam(LPSolutionParams.TIME, Long.class);
  }

  public double getObjectiveValue() throws LPModelException {
    return getSolutionParam(LPSolutionParams.OBJECTIVE, Double.class);
  }

  public double getMipGap() throws LPModelException {
    return getSolutionParam(LPSolutionParams.MIP_GAP, Double.class);
  }

  private <T> T getSolutionParam(LPSolutionParams param, Class<T> instance) throws LPModelException {
    Object paramVal = getModelSolutionParams().get(param);
    if (paramVal!=null) {
      if (instance.isAssignableFrom(paramVal.getClass())){
        return (T) paramVal;
      } else {
        throw new LPModelException("Invalid parameter value assigned to " + param.getDescription());
      }
    } else
      throw new LPModelException(param.getDescription() + " not found in model");
  }

  //Initialize relevant entities in order
  public void init() throws LPModelException {
    this.initModel();
    this.initConstantGroups();
    this.initVarGroups();
    this.initVars();
    this.initConstraintGroups();
    this.initConstraints();
    this.initObjFunctionGenerator();
  }

}
