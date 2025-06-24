# HiGHS Solver

The HiGHS solver converts the programmed model to be solved using the
open-source [HiGHS](https://highs.dev) solver, which is an open source solver.
The module uses generated bindings to the HiGHS C API in the [
`highs-ffm`](../highs-ffm/README.md) module to initialize the model and call the
HiGHS solver from Java/Kotlin.

## Installation Instructions

If you just want to use a default instance of the HiGHS solver, we
recommend [using the docker image](../dev-env-fedora/README.md) packaged with
the binary as it comes with all paths configured appropriately for compilation.
If you wish to continue with a local installation, follow the instructions in
the [highs-ffm](../highs-ffm/README.md) to install HiGHS.

After this, configure the property `highs-root.path` in the project POM to
support compilation and integration testing.

## Running Problem instances

The HiGHS solver needs the references to the HiGHS library path via the
`LD_LIBRARY_PATH` environment variable. These parameters are set to the default
values for linux in the Maven pom.xml as properties, but in order to run problem
instances from compiled code, you need to set the environment variable

```
export LD_LIBRARY_PATH=/opt/HiGHS/build/lib64
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
