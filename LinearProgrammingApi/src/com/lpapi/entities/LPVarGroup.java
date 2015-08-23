package com.lpapi.entities;

import com.lpapi.entities.group.LPGroupInitializer;
import com.lpapi.entities.group.LPNameGenerator;

public final class LPVarGroup extends LPGroup {

  protected LPVarGroup(LPModel model, String identifier, String description, LPNameGenerator<?> generator, LPGroupInitializer initializer) {
    super(model, identifier, description, generator, initializer);
  }
}
