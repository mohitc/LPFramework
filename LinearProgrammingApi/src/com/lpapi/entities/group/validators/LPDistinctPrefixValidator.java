package com.lpapi.entities.group.validators;


import com.lpapi.entities.group.LPNamePrefixValidator;
import com.lpapi.exception.LPNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//Check if two prefixes x and y are not equal to each other
public class LPDistinctPrefixValidator implements LPNamePrefixValidator {

  private static final Logger log = LoggerFactory.getLogger(LPDistinctPrefixValidator.class);

  private int index1, index2, maxIndex;

  private String message;

  public LPDistinctPrefixValidator(int index1, int index2) {
    init(index1, index2, null);
  }

  public LPDistinctPrefixValidator(int index1, int index2, String message) {
    init(index1, index2, message);
  }

  private void init(int index1, int index2, String customMessage) {
    if (index1 < 0) {
      log.error("Validator initialized with invalid index : " + index1 + ", defaulting to 0");
      this.index1 = 0;
    } else {
      this.index1 = index1;
    }
    if (index2 < 0) {
      log.error("Validator initialized with invalid index : " + index2 + ", defaulting to 0");
      this.index2 = 0;
    } else {
      this.index2 = index2;
    }
    maxIndex = (index1 < index2) ? index2 : index1;
    if (customMessage == null) {
      message = "Object at index " + index1 + " and " + index2 + " are the same";
    } else {
      this.message = customMessage;
    }
  }

  @Override
  public void validate(List objects) throws LPNameException {
    if ((objects == null) || (objects.size() < maxIndex) || (objects.get(index1) == null) || (objects.get(index2) == null) || (objects.get(index1).equals(objects.get(index2)))) {
      throw new LPNameException(message);
    }
  }
}