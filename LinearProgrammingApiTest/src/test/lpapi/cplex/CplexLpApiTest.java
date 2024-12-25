package test.lpapi.cplex;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.cplex.impl.CplexLPModel;
import com.lpapi.exception.LPModelException;
import test.lpapi.LPModelTest;

public class CplexLpApiTest extends LPModelTest {

  private static LPModel _instance;

  @Override
  public LPModel getLpModel() throws LPModelException {
    if (_instance==null)
      _instance = new CplexLPModel("Test Instance");
    return _instance;
  }

  public static void main(String[] args) {
    CplexLpApiTest test = new CplexLpApiTest();
    test.testLpModel();
    try {
      System.out.println(test.getLpModel().getObjFn());
      System.out.println(test.getLpModel().getObjectiveValue());
      System.out.println(test.getLpModel().getMipGap());
      System.out.println(test.getLpModel().getSolutionStatus());
      System.out.println(test.getLpModel().getComputationTime());
    } catch (LPModelException e) {
      e.printStackTrace();
    }
  }

}
