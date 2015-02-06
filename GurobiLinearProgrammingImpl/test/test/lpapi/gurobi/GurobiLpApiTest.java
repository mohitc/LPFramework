package test.lpapi.gurobi;

import com.lpapi.entities.LPModel;
import com.lpapi.exception.LPModelException;
import com.lpapi.entities.gurobi.impl.GurobiLPModel;
import test.lpapi.LPModelTest;

public class GurobiLpApiTest extends LPModelTest {

  @Override
  public LPModel getLpModel() throws LPModelException {
    return new GurobiLPModel("Test Instance");
  }

  public static void main(String[] args) {
    GurobiLpApiTest test = new GurobiLpApiTest();
    test.testLpModel();
  }
}
