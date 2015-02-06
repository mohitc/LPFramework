package com.lpapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LPException extends Exception {

  private static final Logger log = LoggerFactory.getLogger(LPModelException.class);

  private String message;

  public LPException(String message) {
    this.message = message;
    if (log.isDebugEnabled()) {
      log.debug(this.getClass().getSimpleName() + ": " + message);
    }
  }

  public String getMessage() {
    return message;
  }
}