package test.lpapi.gurobi;

import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPModelException;
import com.lpapi.entities.gurobi.impl.GurobiLPModel;
import test.lpapi.LPModelTest;

public class GurobiLpApiTest extends LPModelTest {

  private static LPModel _instance;

  @Override
  public LPModel getLpModel() throws LPModelException {
    if (_instance==null)
      _instance = new GurobiLPModel("Test Instance");
    return _instance;
  }

  public static void main(String[] args) {
    GurobiLpApiTest test = new GurobiLpApiTest();
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
