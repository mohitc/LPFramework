package com.lpapi.entities.group.generators;

import com.lpapi.entities.group.LPNameGenerator;
import com.lpapi.exception.LPNameException;
import com.lpapi.exception.LPNameParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public abstract class LPNameGeneratorImpl<T> implements LPNameGenerator <T> {

  protected String prefix;

  protected int indexCount;

  protected String separator = "-/-";

  private static final Logger log = LoggerFactory.getLogger(LPNameGeneratorImpl.class);

  public LPNameGeneratorImpl(String prefix, int indexCount) {
    if ((prefix==null) || (prefix.trim().length()==0)) {
      log.info(this.getClass().getSimpleName() + " initialized with an empty prefix");
      this.prefix = "";
    } else {
      this.prefix = prefix.trim();
    }
    if (indexCount<=0) {
      log.info(this.getClass().getSimpleName() + " initialized with index count <= 0. Resetting to 0");
      this.indexCount = 0;
    } else {
      this.indexCount = indexCount;
    }
  }

  public LPNameGeneratorImpl(String prefix, int indexCount, String separator) {
    if ((prefix==null) || (prefix.trim().length()==0)) {
      log.info(this.getClass().getSimpleName() + " initialized with an empty prefix");
      this.prefix = "";
    } else {
      this.prefix = prefix.trim();
    }
    if (indexCount<=0) {
      log.info(this.getClass().getSimpleName() + " initialized with index count <= 0. Resetting to 0");
      this.indexCount = 0;
    } else {
      this.indexCount = indexCount;
    }
    if ((separator==null) || (separator.trim().length()==0)) {
      log.info(this.getClass().getSimpleName() + " initialized with an empty separator. Reverting to default");
    } else {
      this.separator = separator;
    }
  }


  @Override
  public String getName(T... objects) throws LPNameException {
    validatePrefixes(objects);

    //Generate name String
    String out = prefix;
    for (int i=0;i<indexCount;i++) {
      out = out + separator + objects[i];
    }
    return out;
  }

  protected void validatePrefixes(T... objects) throws LPNameException {
    if (((objects==null) && (indexCount>0)) || (objects.length!=indexCount)){
      throw new LPNameParamException("Number of parameters provided does not match index count " + indexCount);
    }
    List<T> outList = Arrays.asList(objects);
    validatePrefixConstraint(outList);
  }

  protected abstract void validatePrefixConstraint(List<T> objects) throws LPNameException;

}
