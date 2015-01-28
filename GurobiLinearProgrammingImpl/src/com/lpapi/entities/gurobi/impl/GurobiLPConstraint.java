/*
 *  Copyright 2013 ADVA Optical Networking SE. All rights reserved.
 *
 *  Owner: mchamania
 *
 *  $Id: $
 */
package com.lpapi.entities.gurobi.impl;

import com.lpapi.entities.LPConstraint;
import com.lpapi.entities.LPExpression;
import com.lpapi.entities.LPOperator;
import com.lpapi.entities.exception.LPConstraintException;
import gurobi.GRBConstr;
import gurobi.GRBModel;

public class GurobiLPConstraint extends LPConstraint<GRBConstr> {

  GRBConstr modelConstr;

  public GurobiLPConstraint(LPExpression lhs, LPOperator operator, LPExpression rhs) throws LPConstraintException {
    super(lhs, operator, rhs);
  }

  @Override
  protected GRBConstr getModelConstraint() {
    return modelConstr;
  }

  @Override
  protected GRBConstr initModelConstraint() {
    GRBModel model;
//    GRBConstr constr = new model.addConstr()
    return null;
  }
}
