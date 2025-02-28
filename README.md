# Linear Programming Framework

This project is an attempt to generate a common API for modeling (mixed integer) linear programs in Java/Kotlin, which
can then be solved using multiple open-source or commercial solvers. The project currently supports the following
solvers:

* [Gurobi](https://www.gurobi.com/)
* [IBM ILOG Cplex](https://www.ibm.com/products/ilog-cplex-optimization-studio)
* [GLPK](https://www.gnu.org/software/glpk/)
* [SCIP](https://www.scipopt.org/)

## How it works

The project defines a generic API to define a linear optimization problem. The problem is encapsulated in an LPModel
object, which can be initialized independently of the solver in question.

```kotlin
val model = LPModel("Test Instance")
```

The syntax supports three types of variables:

* Boolean (Integer variables with only 0 or 1)
* Integer
* Double

and variables can be defined as follows:

```kotlin
model.variables.add(LPVar("X", LPVarType.BOOLEAN))         // variable defined with default bounds
model.variables.add(LPVar("Y", LPVarType.INTEGER, 0, 10))  // variables defined with explicit bounds
```

Variables can also be grouped into specific categories with string keys, which makes it easier to access them at a
later point in time:

```kotlin 
model.variables.add("some-group", LPVar("Z", LPVarType.DOUBLE, -2.5, 6))
```

Constraints can be defined by setting up terms on the Left/Right-Hand Side (LHS) expressions, coupled with an operator:

```kotlin
// Constraint : X + Y >= 2Z + 3
val c = LPConstraint("Constraint 1")
c.lhs
  .addTerm("X")
  .addTerm("Y")
c.rhs
  .addTerm(2, "Z")
  .add(3)
c.operator = LPOperator.GREATER_EQUAL
model.constraints.add(c)
```

The model also has the option to use named constants. Constraints can be defined against named constants, and their
values can be specified at a later point in time:

```kotlin
// Constraint 2 : aX + bY + cZ <= 4
val c = LPConstraint("Constraint 2")
c.lhs
  .addTerm("a", "X")
  .addTerm("b", "Y")
  .addTerm("c", "Z")
c.rhs.add(4)
c.operator = LPOperator.LESS_EQUAL
model.constraints.add(c)

// Add constants
model.constants.add(LPConstant("a", 1))
model.constants.add(LPConstant("b", 2))
model.constants.add(LPConstant("c", 3))
```

Finally, objective functions can be defined as an expression, and an optimization direction (Minimize / Maximize):

```kotlin
// Objective function => Maximize : X + Y + 2Z
model.objective.expression
  .addTerm("X")
  .addTerm("Y")
  .addTerm(2, "Z")
model.objective.objective = LPObjectiveType.MAXIMIZE
```

In order to solve a model, include an instance of the [lp-solver](lp-solver/README.md) in the classpath as a runtime
dependency, and solve a model as:

```kotlin
val solver = Solver.create(model)
solver.initialize()
val status = solver.solve()
```

## Project Structure

The core of the Linear Programming framework is divided into three modules:

* [lp-api](lp-api/README.md) defines the constructs used to describe a linear optimization problem.
* [lp-solver](lp-solver/README.md) is an abstract interface to a solver, which is implemented for
  - [gurobi-solver](gurobi-solver/README.md)
  - [cplex-solver](cplex-solver/README.md)
  - [glpk-solver](glpk-solver/README.md)
  - [scip-solver](scip-solver/README.md)
* [lp-rw](lp-rw/README.md) implements mechanisms to import/export models and computed results to different file formats.

Additionally, some sample problem instances can be found in the [lp-solver-sample](lp-solver-sample/README.md) module.

## Building the Project

The project uses the Maven build system and depending on the different solvers, uses dependencies to auto-generate code.
Specifically, for all open-source solvers the project uses the Java Foreign Function and Memory (FFM) interface to
create Java bindings for the native C libraries via JExtract.

### Install and configure JExtract

In order to generate this code, you will need to install
[JExtract](https://jdk.java.net/jextract/) which comes as a pre-built binary for most environments. Once it is
extracted, update the environment variable

```xml
<jextract.executable>/opt/jextract/bin/jextract</jextract.executable>
```

in the pom.xml file to the location of the jextract binary installation. At this point, the maven builds of the
relevant modules can use JExtract to generate the Java to C FFM bindings.

### Installing specific solvers

Instructions for building the libraries from scratch are provided in detail in each of the solvers README files. If you
want to build the project for specific solvers only, comment out the modules with the `-solver` and `-ffm` suffix for
all the other solvers in the root pom.xml file. 