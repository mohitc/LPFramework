package com.lpapi.entities;

import com.lpapi.exception.LPModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LPObjFnGenerator {

  protected static final Logger log = LoggerFactory.getLogger(LPObjFnGenerator.class);

  private LPModel model;

  private LPObjType objType;

  public abstract  LPExpression generate() throws LPModelException;

  public LPObjFnGenerator(LPObjType objType) {
    this.objType = objType;
  }

  public LPObjType getObjType() {
    return objType;
  }

  public void setObjType(LPObjType objType) {
    this.objType = objType;
  }

  public LPModel getModel() {
    return model;
  }

  public void setModel(LPModel model) {
    this.model = model;
  }

}
