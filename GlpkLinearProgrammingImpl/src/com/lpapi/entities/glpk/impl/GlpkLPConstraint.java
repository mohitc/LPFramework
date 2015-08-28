/*
 *  Copyright 2013 ADVA Optical Networking SE. All rights reserved.
 *
 *  Owner: mchamania
 *
 *  $Id: $
 */
package com.lpapi.entities.glpk.impl;

import com.lpapi.entities.*;
import com.lpapi.exception.LPConstraintException;
import com.lpapi.exception.LPModelException;
import org.gnu.glpk.*;

import java.util.List;

public class GlpkLPConstraint extends LPConstraint<Integer> {

  private int modelIndex;

  public GlpkLPConstraint(LPModel model, String identifier, LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    super(model, identifier, lhs, operator, rhs);
  }

  @Override
  protected Integer getModelConstraint() {
    return modelIndex;
  }

  @Override
  protected void initModelConstraint() throws LPModelException {
    //add code to generate constraint based on the index provided in the model
    //add variable to model
    try {
      glp_prob lp = ((GlpkLPModel) getModel()).getModel();
      modelIndex = GLPK.glp_add_rows(lp, 1);
      GLPK.glp_set_row_name(lp, modelIndex, getIdentifier());
      //Simplify expression (all variables on the LHS, all constants on the RHS)
      LPExpression newLhs = getLhs().createCopy();
      newLhs.add(getRhs().createCopy().multiply(-1));
      newLhs.reduce();

      // new RHS
      double constantContribution = newLhs.getConstantContribution() * -1;

      //set bounds
      switch (getOperator()) {
        case LESS_EQUAL:
          GLPK.glp_set_row_bnds(lp, modelIndex, GLPKConstants.GLP_UP, 0, constantContribution);
          break;
        case GREATER_EQUAL:
          GLPK.glp_set_row_bnds(lp, modelIndex, GLPKConstants.GLP_LO, constantContribution, 0);
          break;
        case EQUAL:
          GLPK.glp_set_row_bnds(lp, modelIndex, GLPKConstants.GLP_FX, constantContribution, constantContribution);
          break;
        default:
          throw new LPModelException("Operation for operator type " + getOperator() + " not defined");
      }
      //simplified expression available in newLHS, newRHS
      SWIGTYPE_p_int ind;
      SWIGTYPE_p_double val;

      //initialize memory
      List<LPExpressionTerm> termList = newLhs.getTermList();
      int varCount = 1;
      for (LPExpressionTerm term: termList) {
        if (!term.isConstant()) {
          varCount++;
        }
      }
      ind = GLPK.new_intArray(varCount);
      val = GLPK.new_doubleArray(varCount);
      int currColInRow = 1;
      for (LPExpressionTerm term: termList) {
        if (!term.isConstant()) {
          GLPK.intArray_setitem(ind, currColInRow, ((GlpkLPVar)term.getVar()).getModelVar());
          GLPK.doubleArray_setitem(val, currColInRow, term.getCoefficient());
          currColInRow++;
        }
      }
      GLPK.glp_set_mat_row(lp, modelIndex, varCount-1, ind, val);
      GLPK.delete_intArray(ind);
      GLPK.delete_doubleArray(val);
    } catch (Exception e) {
      log.error("Error while initializing LP Variable" , e);
      throw new LPModelException("Error whilie initializing LP Variable: " + e.getMessage());
    }
  }
}
