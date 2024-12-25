package test.lpapi.groups;

import com.lpapi.entities.*;
import com.lpapi.entities.group.LPGroupInitializer;
import com.lpapi.entities.group.LPNameGenerator;
import com.lpapi.entities.group.generators.LPNameGeneratorImpl;
import com.lpapi.entities.group.validators.LPNumberRangeValidator;
import com.lpapi.entities.group.validators.LPPrefixClassValidator;
import com.lpapi.entities.group.validators.LPSetContainmentValidator;
import com.lpapi.entities.skeleton.impl.SkeletonLPModel;
import com.lpapi.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LPGroupTest {

  private static final Logger log = LoggerFactory.getLogger(LPGroupTest.class);

  private class VarGroupInitializer extends LPGroupInitializer {

    private String varName;

    public VarGroupInitializer(String varName) {
      this.varName = varName;
    }

    @Override
    public void run() throws LPModelException {
      model().createLPVar(varName, LPVarType.BOOLEAN, 0, 1, (LPVarGroup)this.getGroup());
    }
  }


  private class ConstantPrefixNameGenerator extends LPNameGeneratorImpl {

    public ConstantPrefixNameGenerator (List<String> varName, int eqCount) {
      super("C", 2);
      addValidator(new LPPrefixClassValidator(0, String.class));
      Set<String> values = new HashSet<>();
      values.addAll(varName);
      addValidator(new LPSetContainmentValidator(0, values));

      addValidator(new LPPrefixClassValidator(1, Integer.class));
      addValidator(new LPNumberRangeValidator(1, 1, eqCount));
    }

    @Override
    protected void validatePrefixConstraint(List objects) throws LPNameException {
    }
  }

  private class ConstantPrefixGroupInitialiazer extends LPGroupInitializer {
    @Override
    public void run() throws LPModelException {
      try {
        model().createLpConstant(generator().getName("X", 1), 1, (LPConstantGroup)getGroup());
        model().createLpConstant(generator().getName("Y",1), 2, (LPConstantGroup)getGroup());
        model().createLpConstant(generator().getName("Z",1), 3, (LPConstantGroup)getGroup());
        model().createLpConstant(generator().getName("X",2), 1, (LPConstantGroup)getGroup());
        model().createLpConstant(generator().getName("Y",2), 1, (LPConstantGroup)getGroup());
        model().createLpConstant(generator().getName("Z",2), 0, (LPConstantGroup)getGroup());
      } catch (LPNameException e) {
        throw new LPModelException(e.getMessage());
      }
    }
  }

  private class ConstantValNameGenerator extends LPNameGeneratorImpl<Integer> {

    public ConstantValNameGenerator (int eqCount) {
      super("Cout", 1);
      //object at index 0 should be between 1 and eqCount
      addValidator(new LPNumberRangeValidator(0, 1, eqCount));
    }

    @Override
    protected void validatePrefixConstraint(List objects) throws LPNameException {
    }
  }

  private class ConstantValGroupIntiializer extends LPGroupInitializer {

    @Override
    public void run() throws LPModelException {
      try {
        model().createLpConstant(generator().getName(1), 4, (LPConstantGroup)getGroup());
        model().createLpConstant(generator().getName(2), 1, (LPConstantGroup)getGroup());
      } catch (LPNameException e) {
        throw new LPModelException(e.getMessage());
      }
    }
  }


  private class ConstraintNameGenerator extends LPNameGeneratorImpl<Integer> {

    public ConstraintNameGenerator(int eqCount) {
      super("Constraint", 1);
      //object at index 0 should be between 1 and eqCount
      addValidator(new LPNumberRangeValidator(0,1,eqCount));
    }

    @Override
    protected void validatePrefixConstraint(List<Integer> objects) throws LPNameException {
    }
  }


  private class ConstraintGroupInitializer extends LPGroupInitializer {

    private LPNameGenerator constantPrefixGenerator, constantValNameGenerator;

    private List<String> varList;

    private int eqCount;

    public ConstraintGroupInitializer(LPNameGenerator constantPrefixGenerator, LPNameGenerator constantValNameGenerator,
                                     List<String> varList, int eqCount) {
      this.constantPrefixGenerator = constantPrefixGenerator;
      this.constantValNameGenerator = constantValNameGenerator;
      this.varList = varList;
      this.eqCount = eqCount;
    }

    @Override
    public void run() throws LPModelException {

      try {
        for (int i=1;i<=eqCount;i++) {
          LPOperator operator = (i==1)?LPOperator.LESS_EQUAL:LPOperator.GREATER_EQUAL;

          LPExpression rhs = new LPExpression(model());
          rhs.addTerm(model().getLPConstant(constantValNameGenerator.getName(i)));

          LPExpression lhs = new LPExpression(model());
          for (String varName: varList) {
            lhs.addTerm(model().getLPConstant(constantPrefixGenerator.getName(varName, i)), model().getLPVar(varName));
          }

          model().addConstraint(generator().getName(i), lhs, operator, rhs, (LPConstraintGroup) getGroup());
        }
      } catch (LPNameException e) {
        throw new LPModelException(e.getMessage());
      }


    }
  }

  private class ObjFunGenerator extends LPObjFnGenerator {

    public ObjFunGenerator(LPObjType objType) {
      super(objType);
    }

    @Override
    public LPExpression generate() throws LPModelException {
      LPExpression expression = new LPExpression(getModel());
      expression.addTerm(1, getModel().getLPVar("X"));
      expression.addTerm(1, getModel().getLPVar("Y"));
      expression.addTerm(2, getModel().getLPVar("Z"));
      return expression;
    }
  }

  private LPModel getModel() throws LPModelException {
    return new SkeletonLPModel("test");
  }


  public void initAndRunModel() throws LPModelException {
    LPModel model = getModel();

    int eqCount = 2;

    List<String> varNameList = new ArrayList<>();
    varNameList.add("X");
    varNameList.add("Y");
    varNameList.add("Z");

    //Constants for prefixes of


    //create LP vars and groups
    for (String varName : varNameList) {
      model.createLPVarGroup(varName, varName + " variable group", null, new VarGroupInitializer(varName));
    }

    //create constant prefix group
    LPNameGenerator constantPrefixNameGenerator = new ConstantPrefixNameGenerator(varNameList, 2);
    model.createLPConstantGroup("ConstantPrefix", "Constant Prefixes for variables in equations", constantPrefixNameGenerator, new ConstantPrefixGroupInitialiazer());

    LPNameGenerator constantValNameGenerator = new ConstantValNameGenerator(eqCount);
    model.createLPConstantGroup("ConstantVas", "Values of expressions for costants", constantValNameGenerator, new ConstantValGroupIntiializer());

    //initialize constraints
    model.createLPConstraintGroup("Constraints", "all Constraints for model", new ConstraintNameGenerator(eqCount),
      new ConstraintGroupInitializer(constantPrefixNameGenerator, constantValNameGenerator, varNameList, eqCount));

    model.attachObjectiveFunctionGenerator(new ObjFunGenerator(LPObjType.MAXIMIZE));

    model.init();
    model.computeModel();
  }

  public static void main(String[] args) {
    LPGroupTest test = new LPGroupTest();
    try {
      test.initAndRunModel();
    } catch (LPModelException e) {
      log.error("Exception while running model", e);
    }
  }


}
