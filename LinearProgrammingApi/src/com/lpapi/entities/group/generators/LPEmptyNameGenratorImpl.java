package com.lpapi.entities.group.generators;

import com.lpapi.entities.group.LPNameGenerator;
import com.lpapi.exception.LPNameException;

public class LPEmptyNameGenratorImpl <T> implements LPNameGenerator {

  @Override
  public String getName(Object[] objects) throws LPNameException {
    throw new LPNameException("No valid name generator available");
  }
}
