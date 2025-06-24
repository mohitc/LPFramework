# LPSolver

The lp-solver module is a generic interface for a solver, and instances of the
supported solvers implement this interface to convert and solve for an [
`LPModel`](../lp-api/src/main/kotlin/io/github/mohitc/lpapi/model/LPModel.kt) instance. The
solver is an abstraction allowing users to write the model in the [
`lp-api`](../lp-api/README.md) format, and then solving the model using one of
solver implementations in this project.

## Initializing a Solver

When writing an application to solve an LPModel instance, there are two distinct
mechanisms to initialize the solvers, which are discussed next.

### Solver-Agnostic Initialization

Each solver implementation includes a Java Service Provider interface
implementation (see [
`LPSpi`](src/main/kotlin/io/github/mohitc/lpsolver/spi/LPSpi.kt)) to instantiate
the solver based on a definition of this SPI interface found in the runtime
libraries. What this means is that if you want to solve a model without any
customization to the underlying solver, you can specify your code as

```kotlin
val solver = Solver.create(model)
solver.initialize()
val status = solver.solve()
```

which is completely solver-agnostic. You can then specify runtime dependencies
in the project using the build system. For example, if you are using Maven and
want to use the `glpk-solver` to solve your problem instance, you can include
the following dependencies in your project:

```xml
<!-- LP Solver dependency which is independent of the backing model -->
<dependency>
  <groupId>io.github.mohitc</groupId>
  <artifactId>lp-solver</artifactId>
  <version>${lpapi.version}</version>
</dependency>
        <!-- Runtime dependency for a specific solver instance -->
<dependency>
<groupId>io.github.mohitc</groupId>
<artifactId>glpk-solver</artifactId>
<scope>runtime</scope>
<version>${lpapi.version}</version>
</dependency>
```

Note that the code does not reference any specific instance of the
`glpk-solver`, and the chosen solver can be replaced by swapping out the runtime
dependency at any point in time.

### Solver-Specific Initialization

A common use case when using specific solver implementations involves providing
advanced configurations to the solvers. In order to do so, solver instances can
also be initialized directly, and the `getBaseModel()` method in the associated
[`LPSolver`](src/main/kotlin/io/github/mohitc/lpsolver/LPSolver.kt)
implementation can be used to access the underlying solver model.

For example, in order to solver the model using the `glpk-solver`, include the
compile-time dependency:

```xml
<!-- Compile-time Dependency for a specific solver instance -->
<dependency>
  <groupId>io.github.mohitc</groupId>
  <artifactId>glpk-solver</artifactId>
  <version>${lpapi.version}</version>
</dependency>
```

The solver can then be initialized, and solved as:

```kotlin
  val solver = GlpkLpSolver(lpModel)
solver.initialize()
var glpkModel: glp_prob? = solver.getBaseModel()
//  perform any custom configurations to the underlying model after all variables and constraints are initialized
solver.solve()
```

## Implementing Support for a new Solver

In order to support a new solver, a module should implement the abstract class [
`LPSolver`](src/main/kotlin/io/github/mohitc/lpsolver/LPSolver.kt)
implementing the appropriate functions to convert variables and constraints as
defined in the LPModel to associated entities in the solver. In order to support
solver-agnostic initialization, the module should also implement an instance of
the [`LPSpi`](src/main/kotlin/io/github/mohitc/lpsolver/spi/LPSpi.kt) interface,
and include a file named `io.github.mohitc.lpsolver.spi.LPSpi` in the
`META-INF/services` folder with a fully qualified class name to the implemented
`LPSpi` interface. Sample instances of the same can be found in one of the
existing solver implementations.
