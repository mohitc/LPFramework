package test.lpapi;

import com.lpapi.entities.*;
import com.lpapi.exception.LPModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

public abstract class LPModelTest {

  private static LPModel _instance;

  private static final Logger log = LoggerFactory.getLogger(LPModelTest.class);

  private static final String _instance_ID = "Test_Instance";

  public abstract LPModel getLpModel() throws LPModelException;

  public void testLpModel() {
    try {
      LPModel instance = getLpModel();

      log.info("Checking if identifier is assigned correctly");
//      assertTrue(instance.getIdentifier().equals(_instance_ID));

      log.info("Checking if default variable group is created correctly");
//      assertNotNull(instance.getLPVarGroupIDs());
//      assertTrue(instance.getLPVarGroupIDs().size()==1);

      LPVar x = instance.createLPVar("X", LPVarType.BOOLEAN, 0, 1);
      LPVar y = instance.createLPVar("Y", LPVarType.BOOLEAN, 0, 1);
      LPVar z = instance.createLPVar("Z", LPVarType.BOOLEAN, 0, 1);

      LPExpression obj =  new LPExpression(instance);
      obj.addTerm(1, x);
      obj.addTerm(1, y);
      obj.addTerm(2, z);

      instance.setObjFn(obj, LPObjType.MAXIMIZE);

      LPExpression lhs1 = new LPExpression(instance);
      LPExpression rhs1 = new LPExpression(instance);
      LPExpression lhs2 = new LPExpression(instance);
      LPExpression rhs2 = new LPExpression(instance);
      lhs1.addTerm(1, x);
      lhs1.addTerm(2, y);
      lhs1.addTerm(3, z);

      rhs1.addTerm(4);
      instance.addConstraint("Constr1", lhs1, LPOperator.LESS_EQUAL, rhs1);

      lhs2.addTerm(1, x);
      lhs2.addTerm(1, y);
      rhs2.addTerm(1);
      instance.addConstraint("Constr2", lhs2, LPOperator.GREATER_EQUAL, rhs2);

      instance.init();
      instance.computeModel();

    } catch (LPModelException e) {
      fail("Error in creating an LP model instance");
    }
  }

}
