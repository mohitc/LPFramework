package com.lpapi.entities.group;

import com.lpapi.exception.LPNameException;

import java.util.List;

public interface LPNamePrefixValidator {

  void validate(List objects) throws LPNameException;
}
