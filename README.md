# Linear Programming Framework

This project is an attempt to generate a common API for modeling (mixed integer)
linear programs in Java/Kotlin, which can then be solved using multiple
open-source or commercial solvers. The project currently supports the following
solvers:

* [Gurobi](https://www.gurobi.com/)
* [IBM ILOG CPLEX](https://www.ibm.com/products/ilog-cplex-optimization-studio)
* [GLPK](https://www.gnu.org/software/glpk/)
* [SCIP](https://www.scipopt.org/)
* [HIGHS](https://highs.dev)
* [ojAlgo](https://www.ojalgo.org/)

## How it works

The project defines a generic API to define a linear optimization problem. The
problem is encapsulated in an LPModel object, which can be initialized
independently of the solver in question.

```kotlin
val model = LPModel("Test Instance")
```

The syntax supports three types of variables:

* Boolean (Integer variables with only 0 or 1)
* Integer
* Double

and variables can be defined as follows:

```kotlin
model.variables.add(
  LPVar(
    "X",
    LPVarType.BOOLEAN
  )
)         // variable defined with default bounds
model.variables.add(
  LPVar(
    "Y",
    LPVarType.INTEGER,
    0,
    10
  )
)  // variables defined with explicit bounds
```

Variables can also be grouped into specific categories with string keys, which
makes it easier to access them at a later point in time:

```kotlin
model.variables.add("some-group", LPVar("Z", LPVarType.DOUBLE, -2.5, 6))
```

Constraints can be defined by setting up terms on the Left/Right-Hand Side (LHS)
expressions, coupled with an operator:

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

The model also has the option to use named constants. Constraints can be defined
against named constants, and their values can be specified at a later point in
time:

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

Finally, objective functions can be defined as an expression, and an
optimization direction (Minimize / Maximize):

```kotlin
// Objective function => Maximize : X + Y + 2Z
model.objective.expression
  .addTerm("X")
  .addTerm("Y")
  .addTerm(2, "Z")
model.objective.objective = LPObjectiveType.MAXIMIZE
```

In order to solve a model, include an instance of
the [lp-solver](lp-solver/README.md) in the classpath as a runtime dependency,
and solve a model as:

```kotlin
val solver = Solver.create(model)
solver.initialize()
val status = solver.solve()
```

## Project Structure

The core of the Linear Programming framework is divided into three modules:

* [lp-api](lp-api/README.md) defines the constructs used to describe a linear
  optimization problem.
* [lp-solver](lp-solver/README.md) is an abstract interface to a solver, which
  is implemented for
  - [gurobi-solver](gurobi-solver/README.md)
  - [cplex-solver](cplex-solver/README.md)
  - [glpk-solver](glpk-solver/README.md)
  - [scip-solver](scip-solver/README.md)
  - [highs-solver](highs-solver/README.md)
  - [ojalgo-solver](ojalgo-solver/README.md)
* [lp-rw](lp-rw/README.md) implements mechanisms to import/export models and
  computed results to different file formats.
* For projects that do not have Java bindings available, the project includes
  code to generate Java bindings to the native C interfaces using the Java
  Foreign Function and Memory (FFM) Interface at
  - [glpk-ffm](glpk-ffm/README.md)
  - [scip-ffm](scip-ffm/README.md)
  - [highs-ffm](highs-ffm/README.md)

  - Additionally, some sample problem instances can be found in
    the [lp-solver-sample](lp-solver-sample/README.md) module.

## Building the Project

The project uses the Maven build system and depending on the different solvers,
uses dependencies to auto-generate code. Specifically, for all open-source
solvers the project uses the Java Foreign Function and Memory (FFM) interface to
create Java bindings for the native C libraries via JExtract. The build
environment assumes specific paths for the installations of the various
dependencies like JExtract, and the library and header paths for the various
open-source solvers to generate the Java-FFM bindings and running the
integration tests.

In order to have a simplified and consistent BUILD process, the project has a
Docker image available under the
`dev-env-fedora` folder. Follow the instructions in
the [README](./dev-env-fedora/README.md) to setup and build the project in the
Docker dev environment.

If you wish to setup and configure the maven environment for your personal
workspace, follow the instructions below. Note that these instructions were
tested with Fedora 42, and some steps will differ with different operating
systems.

### Step 1: Install and configure JExtract

In order to generate this code, you will need to
install [JExtract](https://jdk.java.net/jextract/) which comes as a pre-built
binary for most environments.

The following commands install JExtract in the location expected by the project

```shell
$ wget https://download.java.net/java/early_access/jextract/22/6/openjdk-22-jextract+6-47_linux-x64_bin.tar.gz -P /tmp && \
  tar -zxvf /tmp/openjdk-22-jextract+6-47_linux-x64_bin.tar.gz -C /opt && \
  ln -s /opt/jextract-22 /opt/jextract
```

If you choose to use a different location, update the environment variable

```xml

<jextract.executable>/opt/jextract/bin/jextract</jextract.executable>
```

in the pom.xml file to the location of the jextract binary installation. At this
point, the maven builds of the relevant modules can use JExtract to generate the
Java to C FFM bindings.

### Step 2: Install GLPK

The GNU Linear Programming Kit is available for download
at https://ftp.gnu.org/gnu/glpk/ and uses the `make` BUILD system to compile the
project. Use the commands below to

* Install the dependencies required to build the project
* Download and unpack GLPK 5.0
* Build and install GLPK 5.0

Installation instructions are available in
the [glpk-ffm](glpk-ffm/README.md#installing-glpk) module.

### Step 3: Install SCIP

The SCIP solver is available at https://www.scipopt.org/ and uses the `cmake`
BUILD system to compile the project. In addition to the SCIP Optimization suite,
the system requires a number of other open-source libraries which might differ
for different operating systems.

Installation instructions are available in
the [scip-ffm](scip-ffm/README.md#installing-scip) module.

### Step 4: Install HiGHS

The HiGHS solver code is available at Github
at https://github.com/ERGO-Code/HiGHS and uses the `cmake` BUILD system to
compile the project.

Installation instructions for the HiGHS solver are available in
the [highs-ffm](highs-ffm/README.md#installing-highs) module.

Once all of these dependencies are installed, you should be able to compile the
complete project using the maven build system.

```shell
$ mvn clean install
```

### Custom Instructions for CPLEX

CPLEX does not publish its Java bindings on maven central, and is not built by
default. Instructions in [cplex-solver](cplex-solver/README.md) documentation
point to the steps required to build the `cplex-solver` instance locally.