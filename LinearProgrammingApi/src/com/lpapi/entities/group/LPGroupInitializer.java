package com.lpapi.entities.group;

import com.lpapi.entities.LPGroup;
import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPModelException;

public abstract class LPGroupInitializer {

  private LPGroup group;

  public void setGroup(LPGroup group) {
    this.group = group;
  }

  public LPGroup getGroup() {
    return group;
  }

  protected LPModel model() {
    return group.getModel();
  }

  protected LPNameGenerator generator() {
    return group.getNameGenerator();
  }


  public abstract void run() throws LPModelException;
}
