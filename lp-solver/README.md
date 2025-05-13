# LPSolver

The lp-solver module is a generic interface for a solver, and instances of the supported solvers implement this
interface to convert and solve for an [`LPModel`](../lp-api/src/main/kotlin/com/lpapi/model/LPModel.kt) instance. The
following solvers are currently supported:

* [Gurobi](https://www.gurobi.com/) : [gurobi-solver](../gurobi-solver/README.md)
* [GLPK](https://www.gnu.org/software/glpk/) : [glpk-solver](../glpk-solver/README.md)
* [ILOG CPLEX](https://www.ibm.com/products/ilog-cplex-optimization-studio) : [cplex-solver](../cplex-solver/README.md)

## Using an Existing Solver

When writing an application to solve an LPModel instance, there are two distinct mechanisms to initialize the solvers,
which are discussed next.

### Solver-Agnostic Initialization

When attempting to solve a model without any customization to the underlying solver,
an [`LPSolver`](src/main/kotlin/io/github/mohitc/lpsolver/LPSolver.kt) instance can be solved by including the solver as a
runtime dependency, and then using the provided Java Service Provider interface
(see [`LPSpi`](src/main/kotlin/io/github/mohitc/lpsolver/spi/LPSpi.kt)) to instantiate the solver found in the runtime.

For example, a runtime Maven Dependency for the [glpk-solver](../glpk-solver/README.md) :

```xml
<!-- LP Solver dependency which is independent of the backing model -->
<dependency>
  <groupId>com.lpapi</groupId>
  <artifactId>lp-solver</artifactId>
  <version>${lpapi.version}</version>
</dependency>
<!-- Runtime dependency for a specific solver instance -->
<dependency>
  <groupId>com.lpapi</groupId>
  <artifactId>glpk-solver</artifactId>
  <scope>runtime</scope>
  <version>${lpapi.version}</version>
</dependency>
```

With these dependencies, an instance of the `LPModel` can be solved as:

```kotlin
val solver = Solver.create(model)
solver.initialize()
val status = solver.solve()
```

Note that the code does not reference any specific instance of the `glpk-solver`, and the chosen solver can be replaced
by swapping out the runtime dependency.

### Solver-Specific Initialization

A common use case when using specific solver implementations involves providing advanced configurations to the solvers.
In order to do so, solver instances can also be initialized directly, and the `getBaseModel()` method in the associated
[`LPSolver`](src/main/kotlin/io/github/mohitc/lpsolver/LPSolver.kt) implementation can be used to access the underlying solver
model.

For example, in order to solver the model using the `glpk-solver`, include the compile-time dependency:

```xml
<!-- Compile-time Dependency for a specific solver instance -->
<dependency>
  <groupId>com.lpapi</groupId>
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

In order to support a new solver, a module should implement the abstract
class [`LPSolver`](src/main/kotlin/io/github/mohitc/lpsolver/LPSolver.kt)
implementing the appropriate functions to convert variables and constraints as defined in the LPModel to associated
entities in the solver. In order to support solver-agnostic initialization, the module should also implement an instance
of the [`LPSpi`](src/main/kotlin/io/github/mohitc/lpsolver/spi/LPSpi.kt) interface, and include a file named `com.lpapi.spi.LPSpi` in
the `META-INF/services` folder with a fully qualified class name to the implemented `LPSpi` interface. Sample instances
of the same can be found in one of the existing solver implementations.
