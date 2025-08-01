# SCIP Solver

The HiGHS solver converts the programmed model to be solved using the
[SCIP](https://www.scipopt.org) solver.
The module uses generated bindings to the SCIP C API in the [
`scip-ffm`](../scip-ffm/README.md) module to initialize the model and call the
SCIP solver from Java/Kotlin.

## Installation Instructions

If you just want to use a default instance of the SCIP solver, we
recommend [using the docker image](../dev-env-fedora/README.md) packaged with
the binary as it comes with all paths configured appropriately for compilation.
If you wish to continue with a local installation, follow the instructions in
the [scip-ffm](../scip-ffm/README.md) to install SCIP.

After this, configure the property `scip-library.path` in the project POM to
support compilation and integration testing.

## Running Problem instances

The SCIP solver needs the references to the SCIP library path via the
`LD_LIBRARY_PATH` environment variable. These parameters are set to the default
values for linux in the Maven pom.xml as properties, but in order to run problem
instances from compiled code, you need to set the environment variable

```
export LD_LIBRARY_PATH=/usr/local/lib64
```

when running the binary.

## Running Integration Tests

Sample problem instances can be found
as [integration tests](./src/integration-test/kotlin) folder and use the
instances in the [`lp-solver-sample`](../lp-solver-sample/README.md) module. In
order to run these instances, just use the command

```shell
mvn clean verify
```

to run the problem instances.
