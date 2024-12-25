/*
 *  Copyright 2013 ADVA Optical Networking SE. All rights reserved.
 *
 *  Owner: mchamania
 *
 *  $Id: $
 */
package test.lpapi.glpk;

import com.lpapi.entities.LPModel;
import com.lpapi.entities.glpk.impl.GlpkLPModel;
import com.lpapi.exception.LPModelException;
import test.lpapi.LPModelTest;

public class GlpkLpApiTest extends LPModelTest {

  private static LPModel _instance;

  @Override
  public LPModel getLpModel() throws LPModelException {
    if (_instance==null)
      _instance = new GlpkLPModel("Test Instance");
    return _instance;
  }

  public static void main(String[] args) {
    GlpkLpApiTest test = new GlpkLpApiTest();
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
