package test.lpapi;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.exception.LPModelException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LPModelTest {

  private static LPModel _instance;

  private static final Logger log = LoggerFactory.getLogger(LPModelTest.class);

  private static final String _instance_ID = "Test_Instance";

  private static LPModel getLpModel() throws LPModelException {
    if (_instance==null)
      _instance = new LPModel(_instance_ID);
    return _instance;
  }

  @Test
  public void testLpModel() {
    try {
      LPModel instance = getLpModel();

      log.info("Checking if identifier is assigned correctly");
      assertTrue(instance.getIdentifier().equals(_instance_ID));

      log.info("Checking if default variable group is created correctly");
      assertNotNull(instance.getLPVarGroupIDs());
      assertTrue(instance.getLPVarGroupIDs().size()==1);


    } catch (LPModelException e) {
      fail("Error in creating an LP model instance");
    }
  }

}
