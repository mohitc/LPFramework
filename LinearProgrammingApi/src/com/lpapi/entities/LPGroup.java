package com.lpapi.entities;

import com.lpapi.entities.group.LPEmptyGroupInitializer;
import com.lpapi.entities.group.LPGroupInitializer;
import com.lpapi.entities.group.LPNameGenerator;
import com.lpapi.entities.group.generators.LPEmptyNameGenratorImpl;
import com.lpapi.exception.LPModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LPGroup {

  private String identifier;

  private String description;

  private LPModel model;

  private LPNameGenerator<?> nameGenerator;

  private LPGroupInitializer initializer;

  private static final Logger log = LoggerFactory.getLogger(LPGroup.class);

  protected LPGroup (LPModel model, String identifier, String description, LPNameGenerator generator, LPGroupInitializer initializer) {
    this.identifier = identifier;
    this.description = description;
    this.model = model;
    if (generator!=null)
      this.nameGenerator = generator;
    else
      this.nameGenerator = new LPEmptyNameGenratorImpl<Object>();
    if (initializer!=null) {
      this.initializer = initializer;
      initializer.setGroup(this);

    } else {
      initializer = new LPEmptyGroupInitializer();
      initializer.setGroup(this);
    }
  }

  public LPNameGenerator getNameGenerator() {
    return this.nameGenerator;
  }

  public void init() throws LPModelException {
    //run the initializer
    if (initializer!=null) {
      log.info("Initializing group: " + this.getClass().getSimpleName() + " / " + this.getIdentifier());
      initializer.run();
    }
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getDescription() {
    return description;
  }

  public LPModel getModel() {
    return this.model;
  }

  public boolean equals(Object o) {
    if (o!=null) {
      if (this.getClass().isAssignableFrom(o.getClass())) {
        return ((LPGroup)o).getIdentifier().equals(this.getIdentifier());
      }
    }
    return false;
  }

  public int hashCode() {
    return identifier.hashCode();
  }

  public String toString() {
    return "(Identifier: " + this.getIdentifier() + ", Description: " + this.getDescription() + ")";
  }

}
