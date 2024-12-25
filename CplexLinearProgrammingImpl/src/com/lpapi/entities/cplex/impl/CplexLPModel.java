package com.lpapi.entities.cplex.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPModelException;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CplexLPModel extends LPModel<IloCplex, IloNumVar, IloConstraint> {

  private IloCplex cplexModel;

  private LPVarFactory<IloNumVar> cplexVarFactory;

  private LPConstraintFactory<IloConstraint> cplexConstraintFactory;

  private Map<LPSolutionParams, Object> solnParams;

  public CplexLPModel(String identifier) throws LPModelException {
    super(identifier);
    cplexVarFactory = new CplexLPVarFactory();
    cplexConstraintFactory = new CplexLPConstraintFactory();
    solnParams = new HashMap<>();

  }

  @Override
  public IloCplex getModel() {
    return cplexModel;
  }

  @Override
  protected LPVarFactory<IloNumVar> getLPVarFactory() {
    return cplexVarFactory;
  }

  @Override
  protected LPConstraintFactory<IloConstraint> getLPConstraintFactory() {
    return cplexConstraintFactory;
  }

  @Override
  public void initModel() throws LPModelException {
    try {
      cplexModel = new IloCplex();
    } catch (IloException e) {
      log.error("Error in generating Cplex LP model", e);
      throw new LPModelException("Error in generating Cplex LP model" + e.getMessage());
    }
  }

  @Override
  public void initObjectiveFunction() throws LPModelException {
    CplexLPExpressionGenerator generator = new CplexLPExpressionGenerator(cplexModel);
    IloNumExpr obj = generator.generateExpression(getObjFn());
    try {
      if (this.getObjType()== LPObjType.MAXIMIZE) {
        cplexModel.addMaximize(obj);
      } else {
        cplexModel.addMinimize(obj);
      }
    } catch (IloException e) {
      log.error("Error in setting objective function for Cplex", e);
      throw new LPModelException("Error in setting objective function for Cplex");
    }

  }

  @Override
  public void computeModel() throws LPModelException {
    try {
      long startTime = System.currentTimeMillis();
      cplexModel.solve();
      long endTime = System.currentTimeMillis();
      IloCplex.Status out = cplexModel.getStatus();
      if (out==IloCplex.Status.Optimal) {
        log.info("Optimal Solution found. Optimal objective: " + cplexModel.getObjValue() + ", MIP Gap: " + cplexModel.getMIPRelativeGap());
        solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.OPTIMAL);
        solnParams.put(LPSolutionParams.OBJECTIVE, cplexModel.getObjValue());
        solnParams.put(LPSolutionParams.MIP_GAP, cplexModel.getMIPRelativeGap());
        solnParams.put(LPSolutionParams.TIME, (endTime - startTime));

      } else if (out == IloCplex.Status.InfeasibleOrUnbounded) {
        log.info("Model is infeasible or unbounded");
        solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.INFEASIBLE_OR_UNBOUNDED);
      } else if (out == IloCplex.Status.Infeasible) {
        log.info("Model is infeasible");
        solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.INFEASIBLE);
      } else if (out == IloCplex.Status.Unbounded) {
        log.info("Model is unbounded");
        solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.UNBOUNDED);
      } else if (out == IloCplex.Status.Bounded || out == IloCplex.Status.Feasible || out == IloCplex.Status.Unknown) {
        log.info("Optimization was stopped with Unknown status (Cplex Status: " + out.toString() + ")");
        solnParams.put(LPSolutionParams.STATUS, LPSolutionStatus.UNKNOWN);
        solnParams.put(LPSolutionParams.OBJECTIVE, cplexModel.getObjValue());
        solnParams.put(LPSolutionParams.MIP_GAP, cplexModel.getMIPRelativeGap());
        solnParams.put(LPSolutionParams.TIME, (endTime - startTime));
      } else {
        //Error
        log.info("Optimization was stopped with Error status (Cplex Status: " + out.toString() + ")");
      }
    } catch (IloException e) {
      log.error("Error while solving CPLEX model", e);
      throw new LPModelException("Error while solving CPLEX model" + e.getMessage());
    }
  }

  @Override
  protected Map<LPSolutionParams, Object> getModelSolutionParams() {
    return Collections.unmodifiableMap(solnParams);
  }
}
