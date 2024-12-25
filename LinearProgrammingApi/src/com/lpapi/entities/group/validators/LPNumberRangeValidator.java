package com.lpapi.entities.group.validators;

import com.lpapi.entities.group.LPNamePrefixValidator;
import com.lpapi.exception.LPNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LPNumberRangeValidator implements LPNamePrefixValidator {

  private static final Logger log = LoggerFactory.getLogger(LPNumberRangeValidator.class);

  private int index;

  private double lBound, uBound;

  private String customMessage;

  public LPNumberRangeValidator(int index, double lBound, double uBound) {
    init(index, lBound, uBound, null);
  }


  public LPNumberRangeValidator(int index, double lBound, double uBound, String message) {
    init(index, lBound, uBound, message);
  }

  private void init(int index, double lBound, double uBound, String customMessage) {
    if (index<0) {
      log.error("Validator initialized with invalid index : " + index + ", defaulting to 0");
      this.index = 0;
    } else {
      this.index = index;
    }
    this.lBound = lBound;
    if (lBound>uBound) {
      log.error("Validator initialized with invalid bounds. Defaulting upper bound to the lower bound = " + lBound);
      this.uBound = lBound;
    } else {
      this.uBound = uBound;
    }
    if (customMessage == null) {
      this.customMessage = "Object at index " + index + " in not within the bounds [" + lBound + ", " + uBound + "]";
    } else {
      this.customMessage = customMessage;
    }
  }

  @Override
  public void validate(List objects) throws LPNameException {
    if ((objects==null) || (objects.size()<index+1) || (objects.get(index) == null) || ( ! Number.class.isAssignableFrom(objects.get(index).getClass()))) {
      throw new LPNameException(customMessage);
    }
    //check if the number is in the bounds
    Number x = (Number) objects.get(index);
    if ((x.doubleValue()<lBound) || (x.doubleValue()>uBound)) {
      throw new LPNameException(customMessage);
    }

  }
}
