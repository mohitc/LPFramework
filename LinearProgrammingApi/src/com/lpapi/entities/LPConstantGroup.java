package com.lpapi.entities;

import com.lpapi.entities.group.LPGroupInitializer;
import com.lpapi.entities.group.LPNameGenerator;

/**
 * Created by mohit on 8/24/15.
 */
public class LPConstantGroup extends LPGroup {

  protected LPConstantGroup(LPModel model, String identifier, String description, LPNameGenerator<?> generator, LPGroupInitializer initializer) {
    super(model, identifier, description, generator, initializer);
  }
}
