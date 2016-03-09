package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPModelException;
import gurobi.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GurobiLPModel extends LPModel<GRBModel, GRBVar, GRBConstr> {

  private GRBEnv grbEnv;

  private GRBModel model;

  private LPVarFactory<GRBVar> grbVarFactory;

  private LPConstraintFactory<GRBConstr> grbConstraintFactory;

  private Map<LPSolutionParams, Object> solnParams;

  public GurobiLPModel(String identifier) throws LPModelException {
    super(identifier);
    grbVarFactory = new GurobiLPVarFactory();
    grbConstraintFactory = new GurobiLPConstraintFactory();
    solnParams = new HashMap<>();
  }

  @Override
  public GRBModel getModel() {
    return model;
  }

  @Override
  protected LPVarFactory<GRBVar> getLPVarFactory() {
    return grbVarFactory;
  }

  @Override
  protected LPConstraintFactory<GRBConstr> getLPConstraintFactory() {
    return grbConstraintFactory;
  }

  @Override
  public void initModel() throws LPModelException {
    try {
      grbEnv = new GRBEnv();
      model = new GRBModel(grbEnv);
    } catch (GRBException e) {
      log.error("Error in generating Gurobi model", e);
      throw new LPModelException("Error in generating Gurobi model" + e.getMessage());
    }
  }

  @Override
  public void initVars() throws LPModelException {
    super.initVars();
    try {
      model.update();
    } catch (GRBException e) {
      log.error("Exception while updating the Gurobi model", e);
      throw new LPModelException("Exception when updating the Gurobi model");
    }
  }

  @Override
  public void initObjectiveFunction() throws LPModelException {
    GurobiLPExpressionGenerator generator = new GurobiLPExpressionGenerator();
    GRBLinExpr obj = generator.generateExpression(getObjFn());
    int objOperator;
    if (this.getObjType()==LPObjType.MAXIMIZE) {
      objOperator = GRB.MAXIMIZE;
    } else {
      objOperator = GRB.MINIMIZE;
    }
    try {
      model.setObjective(obj, objOperator);
    } catch (GRBException e) {
      log.error("Error in setting objective function for Gurobi", e);
      throw new LPModelException("Error in setting objective function for Gurobi");
    }
  }

  @Override
  public void computeModel() throws LPModelException {
    try {
      long startTime = System.currentTimeMillis();
      model.optimize();
      long endTime = System.currentTimeMillis();
      int optimstatus = model.get(GRB.IntAttr.Status);
      switch (optimstatus) {
        case GRB.Status.OPTIMAL:
          log.info("Optimal Solution found. Optimal objective: " + model.get(GRB.DoubleAttr.ObjVal) + ", MIP Gap: " + model.get(GRB.DoubleAttr.MIPGap));
          solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.OPTIMAL);
          solnParams.put(LPSolutionParams.OBJECTIVE, model.get(GRB.DoubleAttr.ObjVal));
          solnParams.put(LPSolutionParams.MIP_GAP, model.get(GRB.DoubleAttr.MIPGap));
          solnParams.put(LPSolutionParams.TIME, (endTime - startTime));
          extractResults();
          break;
        case GRB.Status.INF_OR_UNBD:
          log.info("Model is infeasible or unbounded");
          solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED);
          break;
        case GRB.Status.INFEASIBLE:
          log.info("Model is infeasible");
          solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.INFEASIBLE);
          break;
        case GRB.Status.UNBOUNDED:
          log.info("Model is unbounded");
          solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.UNBOUNDED);
          break;
        case GRB.Status.TIME_LIMIT:
          log.info("Time limit reached");
          solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.TIME_LIMIT);
          solnParams.put(LPSolutionParams.TIME, (endTime - startTime));
          extractResults();
          break;
        case GRB.Status.CUTOFF:
          log.info("Cutoff reached. Objective: " + model.get(GRB.DoubleAttr.ObjVal) + ", MIP Gap: " + model.get(GRB.DoubleAttr.MIPGap));
          solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.CUTOFF);
          solnParams.put(LPSolutionParams.OBJECTIVE, model.get(GRB.DoubleAttr.ObjVal));
          solnParams.put(LPSolutionParams.MIP_GAP, model.get(GRB.DoubleAttr.MIPGap));
          solnParams.put(LPSolutionParams.TIME, (endTime - startTime));
          extractResults();
          break;
        default:
          log.info("Optimization was stopped with unknown status (GRB Status: " + optimstatus + ")");
          solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.UNKNOWN);
          break;
      }
    } catch (GRBException e) {
      log.error("Error in optimizing model", e);
      throw new LPModelException("Error in optimizing model");
    }
  }

  @Override
  public void extractResults() throws LPModelException {
    log.info("Extracting results of computed model into the variables");
    for (String varIdentifier: getLPVarIdentifiers()) {
      LPVar var  = getLPVar(varIdentifier);
      if (var instanceof GurobiLPVar) {
        try {
          var.setResult(((GurobiLPVar)var).getModelVar().get(GRB.DoubleAttr.X));
        } catch (GRBException e) {
          log.error("Error while extracting value of variable " + var, e);
        }
      }
    }
  }

  @Override
  protected Map<LPSolutionParams, Object> getModelSolutionParams() {
    return Collections.unmodifiableMap(solnParams);
  }

}
