# lp-api: API for defining a solver-agnostic linear optimization program

Programs to solve linear optimization problems typically define their own APIs
for describing optimization problems, making it hard to switch solvers. There
are standard formats such as [AMPL](https://ampl.com) but this can be hard to
model in. A key feature of the Linear Optimization framework is a
solver-agnostic model definition API, which is then used by implementations of
the [lp-solver](../lp-solver/README.md) package to generate the solver-specific
model instance.

## Overview: LPModel

Each linear optimization model is defined as a [`LPModel`](#overview-lpmodel)
object, which encapsulates the definitions of
[variables](#variables-lpvar), [constraints](#constraints-lpconstraint),
and [objective functions](#objective-function-lpobjective). A linear
optimization model can be defined as:

```kotlin
model = LPModel("an-example")
// Initializing variables
model.variables.add(LPVar("X", LPVarType.BOOLEAN))
model.variables.add(LPVar("Y", LPVarType.BOOLEAN))
model.variables.add(LPVar("Z", LPVarType.BOOLEAN))

// Specifying an objective function: Max (X + Y + 2Z)
model.objective.expression
  .addTerm("X")
  .addTerm("Y")
  .addTerm(2, "Z")
model.objective.objective = LPObjectiveType.MAXIMIZE

// Add Constraints
// Constraint 1 : 2X + 3Y + 4Z <= 4
val constraint1 = model.constraints.add(LPConstraint("Constraint 1"))
constraint1?.lhs
  ?.addTerm(2, "X")
  ?.addTerm(3, "Y")
  ?.addTerm(4, "Z")
constraint1?.rhs?.add(4)
constraint1?.operator = LPOperator.LESS_EQUAL

// Constraint 2 : X + Y >= 2
val constraint2 = model.constraints.add(LPConstraint("Constraint 2"))
constraint2?.lhs
  ?.addTerm("X")
  ?.addTerm("Y")
constraint2?.rhs?.add(2)
constraint2?.operator = LPOperator.GREATER_EQUAL
```

We now take a look at the different components used to define a linear
optimization formulation. Alternately, readers can also take a look at the
examples provided in the [lp-solve-sample](../lp-solver-sample/README.md)
package. After an overview of the individual components, we also discuss the
functionalities in the model to group the various components of the model in a
meaningful fashion.

### Variables: LPVar

A variable is defined as an instance of the [
`LPVar`](src/main/kotlin/io/github/mohitc/lpapi/model/LPVar.kt) class, and
essentially contains

* A unique immutable String identifier
* An immutable enum declaring a variable as one of (Boolean/Integer/Double)
* Bounds (lower bound / upper bound) on the variable.

A variable can be declared with or without its bounds, which can be set later:

```kotlin
var x = LPVar("x", LPVarType.DOUBLE)
x.bounds(-10.0, 23.0)
var y = LPVar("y", LPVarType.INTEGER, -12, 15)
```

A variable also has a construct to store the computed result as an outcome of
solving the model. The result is set via the instance of the solver, which
defines two parameters:

```kotlin
if (x.resultSet) { // resultSet is a boolean to indicate if the result was set
  log.info(x.result) // the result is stored as a Number, which defaults to 0
}
```

### Named Constants: LPConstant

When defining expressions for objective/constraints in a linear program, it is
often useful to use constants for specifying the model, and then provide the
values of the constants for a specific instance of the problem.  [
`LPConstant`](./src/main/kotlin/io/github/mohitc/lpapi/model/LPConstant.kt)
supports this paradigm by defining named constants, with the option of defining
their values at a later point in time.

```kotlin
var c = LPConstant("c", -10) // Initialize a constant with a value
var d =
  LPConstant("d")      // Initialize a constant without a value (defaults to 0)
d.value = -2.3               // set the value of d at a later point in time.
```

### Expressions: LPExpression

The [`LPExpression`](./src/main/kotlin/io/github/mohitc/lpapi/model/LPExpression.kt) object
is a representation of a generic linear expression, which is a combination of
terms. A term, represented by an [
`LPExpressionTerm`](./src/main/kotlin/io/github/mohitc/lpapi/model/LPExpressionTerm.kt)object
can be a

* constant, represented by a named constant, or a fixed number
* a scaled variable, with the scaling factor defined as a named constant, or a
  fixed value

The `LPExpression` is used to represent [constraints](#constraints-lpconstraint) in the
model, as well as in the definition of the
[objective](#objective-function-lpobjective) functions. A reference example of populating terms in
a constraint is shown below:

```kotlin
// Expression: aX + bY - 3Z + d - 4
// Variables: X, Y, Z
// Named Constants: a, b
val expr = LPExpression()
expr
  .addTerm("a", "X")
  .addTerm("b", "Y")
  .addTerm(-3, "Z")
  .add("d")
  .add(-4)
```

### Constraints: LPConstraint

The [
`LPConstraint`](./src/main/kotlin/io/github/mohitc/lpapi/model/LPConstraint.kt)
object is used to define a constraint imposed on a model. Constraints are
referenced by a unique identifier, and consist of:

* [`LPExpression`](#expressions-lpexpression) terms representing the `lhs`(Left-Hand Side)
  and `rhs`(Right-Hand Side).
* [LPOperator](src/main/kotlin/io/github/mohitc/lpapi/model/enums/LPOperator.kt)
  to define the conditional operations
  (&leq;, =, &geq; ) between the two expressions.

An example of the same is shown below

```kotlin
// Expression: 2X + 3Y - 4Z >= 5
// Variables: X, Y, Z
val c = LPConstraint("sample-constraint")
c.lhs
  .addTerm(2, "X")
  .addTerm(3, "Y")
  .addTerm(-4, "Z")
c.rhs.add(5)
c.operator = LPOperator.GREATER_EQUAL
```

### Objective function: LPObjective

The objective function for a linear optimization problem is defined using a
combination of a

* [`LPExpression`](#expressions-lpexpression) linear expression
* optimization direction (maximize/minimize) defined as
  [LPObjectiveType](src/main/kotlin/io/github/mohitc/lpapi/model/enums/LPObjectiveType.kt)

The [
`LPObjective`](./src/main/kotlin/io/github/mohitc/lpapi/model/LPObjective.kt)
object is used to encapsulate these two values, and can be defined as

```kotlin
objective.expression
  .addTerm("X")
  .addTerm("Y")
  .addTerm(2, "Z")
objective.objective = LPObjectiveType.MAXIMIZE
```

### Computation Results: LPModelResult

There are two distinct components of a solution for a model. The `LPModelResult`
object in the [
`LPModel`](./src/main/kotlin/io/github/mohitc/lpapi/model/LPModel.kt) is used to
define the overall status of the result of a computation, and stores generic
parameters such as the

* status of the computation, enumerated via [
  `LPSolutionStatus`](src/main/kotlin/io/github/mohitc/lpapi/model/enums/LPSolutionStatus.kt)
* computation time
* mip gap, if applicable
* value of the objective function

The result of the computation can be accessed via the `LPModel.solver` field.

In case of a successful computation, the values of the individual variables are
also of interest, and they are stored in the associated `LPVar` objects. The
LPVar object has two distinct fields: `resultSet: Boolean` to indicate that a
result was set for the variable, and the `result: Number` with the associated
value.

## Additional Features

We now describe some additional features of the model, which are useful when
working with large models.

### Component Grouping

Components of the `LPModel`, namely the `LPVar`, `LPConstant` and `LPConstraint`
can grow to be quite large. These components usually have some logical grouping
that is evident to the designers of the models. There is a requirement that all
identifiers associated with the entities are unique, but the model supports a
mechanism to associate these components with a logical group when initializing
the individual entities. The three aforementioned entities all implement the [
`LPParameter`](./src/main/kotlin/io/github/mohitc/lpapi/model/LPParameter.kt)
interface, and are stored in the
`LPModel` as an `LPParameterGroup`, which supports the association of parameters
with logical groups.

The `LPParameterGroup` exposes methods to get all groups registered, and the
identifiers of all parameters.

```kotlin
// variable added to the default grouping
model.variables.add(LPVar("X", LPVarType.BOOLEAN))

// variables added to the group a-logical-group
model.variables.add("a-logical-group", LPVar("Y", LPVarType.BOOLEAN))
model.variables.add("a-logical-group", LPVar("Z", LPVarType.BOOLEAN))

// Example: Get all groups, and iterate over the associated variable identifiers in the group
model.variables.getAllGroups().forEach { id ->
  log.info(
    "Variable Identifiers for Group $id: ${
      model.variables.getAllIdentifiers(
        id
      ).orEmpty()
    }"
  )
}
```

### Expression Reduction

The `LPModel` allows expression of constraints and expressions as a sequence of
terms, and this model can be useful in expressing the model in a programmatic
fashion. However, it might be useful to have simplified linear expression,
especially when generating the matrix notation for linear optimization problems.
The `LPModel` class implements a
`reduce(...)` method, that can be used to reduce `LPExpression`, `LPConstraint`,
and `LPObjective` objects based on the variable and constant definitions in the
model. These reductions are used internally for:

* Pre-computation validation of the model to ensure that all constraints and
  objective functions can be reduced
* Generation of the matrix representation of the model for solvers that use the
  notation for the definition of the models
