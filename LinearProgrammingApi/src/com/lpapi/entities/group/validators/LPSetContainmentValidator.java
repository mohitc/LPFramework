package com.lpapi.entities.group.validators;

import com.lpapi.entities.group.LPNamePrefixValidator;
import com.lpapi.exception.LPNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

//Validator to check if values for an index are contained in a predefined set
public class LPSetContainmentValidator implements LPNamePrefixValidator {

  private static final Logger log = LoggerFactory.getLogger(LPSetContainmentValidator.class);

  private Set valueSet;

  private int index;

  private String message;

  public LPSetContainmentValidator(int index, Set valueSet) {
    init(index, valueSet, null);
  }

  public LPSetContainmentValidator(int index, Set valueSet, String message) {
    init(index, valueSet, message);
  }

  private void init(int index, Set valueSet, String customMessage) {
    if (index<0) {
      log.error("Validator initialized with invalid index : " + index + ", defaulting to 0");
      this.index = 0;
    } else {
      this.index = index;
    }
    if (valueSet == null) {
      log.error("Validator initialized with invalid value set. Defaulting to Empty set");
      this.valueSet= Collections.EMPTY_SET;
    } else {
      this.valueSet = valueSet;
    }
    if (customMessage == null) {
      message = "Object at index " + index + " in not provided value set";
    } else {
      this.message = customMessage;
    }
  }

  @Override
  public void validate(List objects) throws LPNameException {
    if ((objects==null) || (objects.size()<index+1) || (objects.get(index) == null) || ( ! valueSet.contains(objects.get(index)))) {
      throw new LPNameException(message);
    }
  }
}
