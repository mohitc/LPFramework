package com.lpapi.entities;

import com.lpapi.exception.LPModelException;
import com.lpapi.exception.LPVarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LPVar<T> {

  protected static final Logger log = LoggerFactory.getLogger(LPVar.class);

  private static final String marker = "LP-Var: ";

  private LPModel model;

  private String identifier;

  private LPVarType varType;

  private double lBound;

  private double uBound;

  private double result;

  private boolean resultSet = false;

  protected LPVar(LPModel model, String identifier, LPVarType type) throws LPVarException {
    log.debug(marker, "Validating input parameters and generating new LP Var");
    if (model==null) {
      throw new LPVarException("Provided model cannot be null");
    }
    this.model = model;
    if ((identifier==null) || (identifier.equals(""))) {
      throw new LPVarException("Identifier cannot be null or an empty string");
    }
    this.identifier = identifier;
    if (type == null) {
      throw new LPVarException("Variable type cannot be null");
    }
    this.varType = type;
  }

  public String getIdentifier() {
    return identifier;
  }

  public LPVarType getVarType() {
    return varType;
  }

  public double getlBound() {
    return lBound;
  }

  public double getuBound() {
    return uBound;
  }

  public void setBounds(double lBound, double uBound) throws LPVarException {
    if (lBound>uBound)
      throw new LPVarException("Lower bound " + lBound + " cannot be greater than upper bound " + uBound);
    this.lBound = lBound;
    this.uBound = uBound;
  }

  public LPModel getModel() {
    return model;
  }

  public String toString () {
    return "[model: " + model.getIdentifier() + ", identifier: " + identifier + ", type:" + varType + "]";
  }

  public boolean equals(Object o) {
    if (o!=null) {
      if ((LPVar.class.isAssignableFrom(o.getClass())) && (identifier.equals(((LPVar)o).getIdentifier()))) {
        return true;
      }
    }
    return false;
  }

  public int hashCode() {
    return identifier.hashCode();
  }

  public abstract T getModelVar();

  protected abstract void initModelVar() throws LPModelException;

  public double getResult() {
    return result;
  }

  protected void setResult(double result){
    this.result = result;
    this.resultSet = true;
  }

  public boolean isResultSet() {
    return resultSet;
  }
}
