/*
 *  Copyright 2013 ADVA Optical Networking SE. All rights reserved.
 *
 *  Owner: mchamania
 *
 *  $Id: $
 */
package com.lpapi.entities.glpk.impl;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.LPVar;
import com.lpapi.entities.LPVarFactory;
import com.lpapi.entities.LPVarType;
import com.lpapi.exception.LPVarException;

public class GlpkLPVarFactory implements LPVarFactory<Integer> {

  @Override
  public LPVar<Integer> generateLPVar(LPModel model, String identifier, LPVarType type, double lBound, double uBound) throws LPVarException {
    if (model !=null && model instanceof GlpkLPModel) {
      LPVar<Integer> var = new GlpkLPVar(model, identifier, type);
      var.setBounds(lBound,uBound);
      return var;
    }
    throw new LPVarException("Incompatible model type for generating GlpkLPVar");
  }
}
