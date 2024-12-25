package com.lpapi.export;

import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPImportException;

public abstract class LPModelImporter {

  private LPModel model;

  public LPModelImporter(LPModel model) throws LPImportException {
    if (model == null)
      throw new LPImportException("Parameters cannot be imported onto a null model");
    //Model should be empty
    if (model.getLPVarIdentifiers().size()>0)
      throw new LPImportException("Model should be empty before performing an import.");
    if (model.getConstraintList().size()>0)
      throw new LPImportException("Model should be empty before performing an import.");
    this.model = model;
  }

  public LPModel getModel() {
    return model;
  }

  public abstract void importModel();
}
