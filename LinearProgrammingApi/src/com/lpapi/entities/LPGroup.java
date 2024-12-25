package com.lpapi.entities;

public abstract class LPGroup {

  private String identifier;

  private String description;

  protected LPGroup(String identifier, String description) {
    this.identifier = identifier;
    this.description = description;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getDescription() {
    return description;
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
    return "(Identifier: " + identifier + ", Description: " + description + ")";
  }

}
