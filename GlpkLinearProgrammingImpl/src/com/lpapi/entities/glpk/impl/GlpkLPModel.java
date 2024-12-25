package com.lpapi.entities.glpk.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPModelException;
import org.gnu.glpk.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlpkLPModel extends LPModel<glp_prob, Integer, Integer> {

  private GlpkLPVarFactory varFactory;

  private GlpkLPConstraintFactory constraintFactory;

  private glp_prob model;


  public GlpkLPModel(String identifier) throws LPModelException {
    super(identifier);
    this.varFactory = new GlpkLPVarFactory();
    this.constraintFactory = new GlpkLPConstraintFactory();
  }

  @Override
  public glp_prob getModel() {
    return model;
  }

  @Override
  protected LPVarFactory<Integer> getLPVarFactory() {
    return varFactory;
  }

  @Override
  protected LPConstraintFactory<Integer> getLPConstraintFactory() {
    return constraintFactory;
  }

  @Override
  public void initModel() throws LPModelException {
    try {
      model = GLPK.glp_create_prob();
      GLPK.glp_set_prob_name(model, getIdentifier());
    } catch (GlpkException e) {
      log.error("Error in creating a GLPK problem instance", e);
      throw new LPModelException("Error in creating a GLPK problem instance: " + e.getMessage());
    }
  }

  @Override
  public void initObjectiveFunction() throws LPModelException {
    GLPK.glp_set_obj_name(model, "Obj");
    switch (getObjType()) {
      case MINIMIZE:
        GLPK.glp_set_obj_dir(model, GLPKConstants.GLP_MIN);
        break;
      case MAXIMIZE:
        GLPK.glp_set_obj_dir(model, GLPKConstants.GLP_MAX);
        break;
      default:
        throw new LPModelException("GLP constant for type " + getObjType() + " not defined");
    }

    LPExpression newObjective = getObjFn().createCopy().reduce();
    //initialize objective
    List<LPExpressionTerm> termList = newObjective.getTermList();
    for (LPExpressionTerm term: termList) {
      if (term.isConstant()) {
        GLPK.glp_set_obj_coef(model, 0, term.getCoefficient());
      } else {
        GLPK.glp_set_obj_coef(model, ((GlpkLPVar)term.getVar()).getModelVar(), term.getCoefficient());
      }
    }
  }

  @Override
  public void computeModel() throws LPModelException {
    glp_iocp iocp = new glp_iocp();
    GLPK.glp_init_iocp(iocp);
    iocp.setPresolve(GLPKConstants.GLP_ON);
    GLPK.glp_write_lp(model, null, "model.lp");
    GLPK.glp_intopt(model, iocp);

    double val  = GLPK.glp_mip_obj_val(model);
    LPSolutionStatus solnStatus = getSolutionStatus(GLPK.glp_mip_status(model));
    solnParams.put(LPSolutionParams.STATUS, solnStatus);
    if (solnStatus!=LPSolutionStatus.UNKNOWN && solnStatus!=LPSolutionStatus.INFEASIBLE) {
      log.debug("Objective : " + GLPK.glp_get_obj_val(model));
      solnParams.put(LPSolutionParams.OBJECTIVE, val);
      extractResults();
    }
  }

  @Override
  public void extractResults() throws LPModelException {
    log.info("Extracting results of computed model into the variables");
    for (String varIdentifier: getLPVarIdentifiers()) {
      LPVar var  = getLPVar(varIdentifier);
      if (var instanceof GlpkLPVar) {
        try {
          var.setResult(GLPK.glp_mip_col_val(model, ((GlpkLPVar)var).getModelVar()));
        } catch (Exception e) {
          log.error("Error while extracting value of variable " + var, e);
        }
      }
    }
  }


  private LPSolutionStatus getSolutionStatus(int solnStatus) {
    if (solnStatus==GLPKConstants.GLP_UNDEF)
      return LPSolutionStatus.UNKNOWN;
    else if (solnStatus==GLPKConstants.GLP_OPT)
      return LPSolutionStatus.OPTIMAL;
    else if (solnStatus==GLPKConstants.GLP_FEAS)
      return LPSolutionStatus.TIME_LIMIT;
    else if (solnStatus==GLPKConstants.GLP_INFEAS)
      return LPSolutionStatus.INFEASIBLE;

    return LPSolutionStatus.UNKNOWN;
  }


}
