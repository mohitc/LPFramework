package com.lpapi.entities.glpk.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPModelException;
import org.gnu.glpk.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlpkLPModel extends LPModel<glp_prob, Integer, Integer> {

  private GlpkLPVarFactory varFactory;

  private GlpkLPConstraintFactory constraintFactory;

  private glp_prob model;

  private Map<LPSolutionParams, Object> solnParams;

  public GlpkLPModel(String identifier) throws LPModelException {
    super(identifier);
    this.varFactory = new GlpkLPVarFactory();
    this.constraintFactory = new GlpkLPConstraintFactory();
    solnParams = new HashMap<>();

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
//  GLPK.glp_write_lp(lp, null, "yi.lp");
    int ret = GLPK.glp_intopt(model, iocp);

    int i;
    int n;
    String name;
    double val;

    name = GLPK.glp_get_obj_name(model);
    val  = GLPK.glp_mip_obj_val(model);
    System.out.print(name);
    System.out.print(" = ");
    System.out.println(val);
    n = GLPK.glp_get_num_cols(model);
    for(i=1; i <= n; i++)
    {
      name = GLPK.glp_get_col_name(model, i);
      val  = GLPK.glp_mip_col_val(model, i);
      log.debug(name + " = " + val);
    }

  }

  @Override
  protected Map<LPSolutionParams, Object> getModelSolutionParams() {
    return null;
  }
}
