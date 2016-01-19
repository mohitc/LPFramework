package com.lpapi.entities.group.validators;

import com.lpapi.entities.group.LPNamePrefixValidator;
import com.lpapi.exception.LPNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//Check if a prefix is an instance of a specific class type
public class LPPrefixClassValidator implements LPNamePrefixValidator {

  private static final Logger log = LoggerFactory.getLogger(LPPrefixClassValidator.class);

  private Class instance;

  private int index;

  private String message;

  public LPPrefixClassValidator(int index, Class instance) {
    init(index, instance, null);
  }

  public LPPrefixClassValidator(int index, Class instance, String message) {
    init(index, instance, message);
  }

  private void init(int index, Class instance, String customMessage) {
    if (index<0) {
      log.error("Validator initialized with invalid index : " + index + ", defaulting to 0");
      this.index = 0;
    } else {
      this.index = index;
    }
    if (instance == null) {
      log.error("Validator initialized with invalid null class instance. Defaulting to Object");
      this.instance = Object.class;
    } else {
      this.instance = instance;
    }
    if (customMessage == null) {
      message = "Object at index " + index + " in not a valid instance of class " + instance.getSimpleName();
    } else {
      this.message = customMessage;
    }
  }

  @Override
  public void validate(List objects) throws LPNameException {
    if ((objects==null) || (objects.size()<index+1) || (objects.get(index) == null) || ( ! instance.isAssignableFrom(objects.get(index).getClass()))) {
      throw new LPNameException(message);
    }
  }
}
