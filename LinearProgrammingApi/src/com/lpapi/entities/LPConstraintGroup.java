package com.lpapi.entities;

import com.lpapi.entities.group.LPGroupInitializer;
import com.lpapi.entities.group.LPNameGenerator;

public final class LPConstraintGroup extends LPGroup {

  protected LPConstraintGroup(LPModel model, String identifier, String description, LPNameGenerator<?> generator, LPGroupInitializer initializer) {
    super(model, identifier, description, generator, initializer);
  }
}
