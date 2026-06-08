# LPFramework Integration Skill

This skill allows agents to formulate (Mixed Integer) Linear Programming models
using `LPFramework` in Kotlin or Java, and solve them via various open-source /
commercial solvers.

## Metadata

```yaml
name: lpframework-modeling
description: Formulate Mixed Integer Linear Programming (M-ILP) models in Java/Kotlin using `LPFramework` which can be solved using various open source and commercial solvers..
triggers:
  - "define linear program"
  - "integer linear programming"
  - "mixed integer linear programming"
  - "ILP solver-agnostic model"
  - "linear optimization"
```

## Setup & Dependency Configuration

To use the framework, configure the project build (e.g., Maven `pom.xml` or
Gradle `build.gradle.kts`).

### Maven Dependencies

The minimal dependencies required to define a model require importing the core
modeling API and the abstract solver layer.

```xml

<dependencies>
  <!-- Core LP API -->
  <dependency>
    <groupId>io.github.mohitc.lpapi</groupId>
    <artifactId>lp-api</artifactId>
    <version>${lpframework.version}</version>
  </dependency>
  <!-- Abstract Solver SPI -->
  <dependency>
    <groupId>io.github.mohitc.lpsolver</groupId>
    <artifactId>lp-solver</artifactId>
    <version>${lpframework.version}</version>
  </dependency>
</dependencies>
```

A solver can be chosen based on the constraints of the runtime environment and
the availability of commercial solver licenses. Options include

* Open Source Solvers
  - `ojalgo-solver` - [Ojalgo Solver](https://www.ojalgo.org) is a Native Java
    open-source solver no external dependencies
  - `glpk-solver` - Interface to
    the [GNU Linear Programming Kit](https://www.gnu.org/software/glpk/)
  - `scip-solver` - Interface to the [SCIP](https://www.scipopt.org) solver
  - `highs-solver` - Interface to the [HiGHS](https://highs.dev) solver
* Commercial Solvers
  - `gurobi-solver` - Interface to the [Gurobi](https://www.gurobi.com/) solver
    via the official Gurobi Java API.
  - `cplex-solver` - Interface to
    the [IBM ILOG CPLEX](https://www.ibm.com/products/ilog-cplex-optimization-studio)
    solver via the official CPLEX Java API
  - `mosek-solver` - Interface to the [Mosek](https://www.mosek.com/) solver via
    the official Mosek Java API.

The solver dependencies are included as runtime dependencies, and are
automatically picked up by the program via the Java Service Provider Interface (
SPI).

```xml

<dependencies>
  <!-- Solver implementation (e.g., ojalgo) as a runtime dependency -->
  <dependency>
    <groupId>io.github.mohitc.lpsolver</groupId>
    <artifactId>ojalgo-solver</artifactId>
    <version>${lpframework.version}</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

### Pre-configured environments with Open Source Solvers

The `ojalgo-solver` is a Java-native solver and does not require any other
dependencies. For the other solvers, the project provides a docker container
that have the necessary open-source solver installations and configurations in
place. To use this setup, use the following commands

```shell
docker pull ghcr.io/mohitc/lpframework/dev-env-fedora:main
docker run \
  --mount type=bind,src=${path-to-project-root},dst=/root/projects/${project-name} \
  --mount type=bind,src=/home/${user}/.m2,dst=/root/.m2 \
  -it ghcr.io/mohitc/lpframework/dev-env-fedora:main \
  /bin/bash
```

This gives you a shell with your project mounted from your source directory and
comes with the HiGHS, GLPK and SCIP solvers installed.

---

## Core Modeling Guide

### 1. Initializing the Model

Initialize an `LPModel` with a unique identifier:

```kotlin
import io.github.mohitc.lpapi.model.LPModel

val model = LPModel("my-optimization-problem")
```

### 2. Declaring Variables (`LPVar`)

Variables can be BOOLEAN, INTEGER, or DOUBLE.

```kotlin
import io.github.mohitc.lpapi.model.LPVar
import io.github.mohitc.lpapi.model.enums.LPVarType

// Add variables with default bounds ([0.0, 0.0] for Double/Integer, [0, 1] for Boolean)
model.variables.add(LPVar("x", LPVarType.BOOLEAN))

// Add variables with explicit bounds
model.variables.add(LPVar("y", LPVarType.INTEGER, 0, 10))
model.variables.add(LPVar("z", LPVarType.DOUBLE, -5.0, 5.0))

// Add variables with logical grouping categories, useful for categorizing exports
model.variables.add("my-group", LPVar("w", LPVarType.DOUBLE))
```

### 3. Creating Constraints (`LPConstraint`)

Define constraint LHS, RHS, and operator types (LESS_EQUAL, GREATER_EQUAL,
EQUAL):

```kotlin
import io.github.mohitc.lpapi.model.LPConstraint
import io.github.mohitc.lpapi.model.enums.LPOperator

// Constraint: x + 2y <= Capacity
model.constraints.add(
  LPConstraint("CapacityLimit").apply {
    this.lhs.addTerm("x")
    this.lhs.addTerm(2, "y")
    this.rhs.add("Capacity") // Referencing the LPConstant named "Capacity"
    this.operator = LPOperator.LESS_EQUAL
  }
)
```

### 4. Setting Up Constants (`LPConstant`)

Use named constants to keep model definitions generic. Constants can be used as
replacements for numeric values in Constraints and Objective function
expressions:

```kotlin
import io.github.mohitc.lpapi.model.LPConstant

// Define a named constant with a value
model.constants.add(LPConstant("Capacity").value(100.0))

// Define a constant and set its value later
val limit = LPConstant("Limit")
limit.value(50.0)
model.constants.add(limit)

// use a constant in an expression
model.objective.expression
  .addTerm("Limit", "x")
```

### 5. Formulating the Objective Function (`LPObjective`)

Configure the optimization direction (MAXIMIZE / MINIMIZE) and terms:

```kotlin
import io.github.mohitc.lpapi.model.enums.LPObjectiveType

// Maximize: 3x + 4y
model.objective.objective = LPObjectiveType.MAXIMIZE
model.objective.expression
  .addTerm(3, "x")
  .addTerm(4, "y")
```

### 6. Validation

Validate the model definition before solving to ensure all variables/constants
referenced in expressions are registered:

```kotlin
val isValid = model.validate()
if (!isValid) {
  throw IllegalStateException("Model validation failed. Verify all variables and constants are registered in the model.")
}
```

---

## Solving & Retrieving Results

Use the `Solver` factory to create the solver from the `LPModel` configuration:

```kotlin
import io.github.mohitc.lpsolver.spi.Solver
import io.github.mohitc.lpapi.model.enums.LPSolutionStatus

// Instantiate the solver
val solver = Solver.create(model)

// Initialize and solve
if (solver.initialize()) {
  val status = solver.solve()
  if (status == LPSolutionStatus.OPTIMAL) {
    println("Optimal solution found!")
    println("Objective value: ${model.solution?.objective}")

    // Retrieve variable values
    val xVal = model.variables.get("x")?.result
    val yVal = model.variables.get("y")?.result
    println("x = $xVal, y = $yVal")
  } else {
    println("Solving ended with status: $status")
  }
} else {
  println("Failed to initialize the solver. Check native dependencies.")
}
```

---

## Exporting / Importing model definitions.

### Maven Dependencies

In order to export a model, or import a model from a previously exported file,
the `lp-rw`  maven dependency must be included.

```xml
  <!-- Abstract Solver SPI -->
<dependency>
  <groupId>io.github.mohitc.lpsolver</groupId>
  <artifactId>lp-rw</artifactId>
  <version>${lpframework.version}</version>
</dependency>
```

### Importing/Exporting model to File

`LPModelParser` class is used to define the format to read/write the LPModel to
a file.

```kotlin
import  io.github.mohitc.lpapi.model.parser.*

// Initialize parser with a specific format for the output
val parser = LPModelParser(LPModelFormat.YAML)

// Write a model to a file
parser.writeToFile(model, fileName)

// Load a model from a file
val newModel = lpModelParser.readFromFile(fileName)
```

---

## Reference Examples

### Simple Knapsack Problem

```kotlin
import io.github.mohitc.lpapi.model.*
import io.github.mohitc.lpapi.model.enums.*
import io.github.mohitc.lpsolver.spi.Solver

fun solveKnapsack() {
  val model = LPModel("knapsack")
  model.objective.objective = LPObjectiveType.MAXIMIZE

  val values = listOf(5.0, 3.0, 2.0, 7.0)
  val weights = listOf(2.0, 8.0, 4.0, 2.0)
  val capacity = 10.0

  // Variables & constants
  for (i in values.indices) {
    model.variables.add(LPVar("x-$i", LPVarType.BOOLEAN))
    model.constants.add(LPConstant("Val-$i").value(values[i]))
    model.constants.add(LPConstant("W-$i").value(weights[i]))
    model.objective.expression.addTerm("Val-$i", "x-$i")
  }
  model.constants.add(LPConstant("Cap").value(capacity))

  // Constraint: sum(W-i * x-i) <= Cap
  model.constraints.add(
    LPConstraint("WeightLimit").apply {
      for (i in values.indices) {
        this.lhs.addTerm("W-$i", "x-$i")
      }
      this.rhs.add("Cap")
      this.operator = LPOperator.LESS_EQUAL
    }
  )

  if (model.validate()) {
    val solver = Solver.create(model)
    solver.initialize()
    val status = solver.solve()
    println("Status: $status")
    println("Objective: ${model.solution?.objective}")
  }
}
```

---

## Logging

All internal logging in the LPFramework is performed over the `slf4j` facade. In
order to log the output to the standard output, use the `slf4j-simple`
dependency, otherwise consult the [SLF4j](https://slf4j.org) documentation for
more options.

```xml

<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-simple</artifactId>
  <version>${slf4j.version}</version>
  <scope>runtime</scope>
</dependency>
```