package com.lpapi.entities.group;

import com.lpapi.exception.LPNameException;

public interface LPNameGenerator <T>{

  public String getName(T ... objects) throws LPNameException;

}
