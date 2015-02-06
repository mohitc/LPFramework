package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPModelException;
import gurobi.*;

import java.util.List;

public class GurobiLPModel extends LPModel<GRBModel, GRBVar, GRBConstr> {

  private GRBEnv grbEnv;

  private GRBModel model;

  private LPVarFactory<GRBVar> grbVarFactory;

  private LPConstraintFactory<GRBConstr> grbConstraintFactory;

  public GurobiLPModel(String identifier) throws LPModelException {
    super(identifier);
    grbVarFactory = new GurobiLPVarFactory();
    grbConstraintFactory = new GurobiLPConstraintFactory();
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
    GRBLinExpr obj = generateLinearExpression(getObjFn());
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
      model.optimize();
      //TODO set result values to the corresponding variables
    } catch (GRBException e) {
      log.error("Error in optimizing model", e);
      throw new LPModelException("Error in optimizing model");
    }
  }

  private GRBLinExpr generateLinearExpression(LPExpression expression) throws LPModelException {
    GRBLinExpr linExpr = new GRBLinExpr();
    List<LPExpressionTerm> termList = expression.getTermList();
    if ((termList!=null) && (termList.size()!=0)){
      for (LPExpressionTerm term: termList) {
        if (term.isConstant()) {
          linExpr.addConstant(term.getCoefficient());
        } else {
          if ((term.getVar().getModelVar() != null) && (GRBVar.class.isAssignableFrom(term.getVar().getModelVar().getClass()))) {
            linExpr.addTerm(term.getCoefficient(), (GRBVar) term.getVar().getModelVar());
          } else {
            throw new LPModelException("Model variable is either null or is not an instance of GRBVar");
          }
        }
      }
    } else {
      throw new LPModelException("Expression is empty");
    }
    return linExpr;
  }
}
