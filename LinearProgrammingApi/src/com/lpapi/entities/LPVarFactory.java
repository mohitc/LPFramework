package com.lpapi.entities;

import com.lpapi.entities.exception.LPVarException;

public interface LPVarFactory <T> {

  public LPVar<T> generateLPVar(LPModel model, String identifier, LPVarType type, double lBound, double uBound) throws LPVarException;
}
