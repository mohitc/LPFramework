package com.lpapi.entities;

public class LPVarGroup {

  private String identifier;

  private String description;

  protected LPVarGroup(String identifier, String description) {
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
      if (LPVarGroup.class.isAssignableFrom(o.getClass())) {
        return ((LPVarGroup)o).getIdentifier().equals(this.getIdentifier());
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
