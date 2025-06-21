# GLPK Solver

The GLPK solver converts the programmed model to be solved using
the [GNU Linear Programming Kit](https://www.gnu.org/software/glpk/)
which is an open source alternative for solving linear programming models. GLPK
is written in C, and in order to call methods from Java/Kotlin, the project uses
Java FFM bindings generated in the [glpk-ffm](../glpk-ffm/README.md) module.

## Installation Instructions

If you just want to use a default instance of the GPLK solver, we
recommend [using the docker image](../dev-env-fedora/README.md) packaged with
the binary as it comes with all paths configured appropriately for compilation.
If you wish to continue with a local installation, follow the instructions in
the [glpk-ffm](../glpk-ffm/README.md) to install GLPK.

The standard GLPK installation in Linux installs the glpk libraries under
`/usr/local/lib`. In case these files are located in a different location,
update the property

```
<ld-library-path>/usr/local/lib</ld-library-path>
```

in the module's [pom.xml](./pom.xml) to compile the module.

## Running Problem instances

The GLPK solver needs the references to the glpk library path via the
`LD_LIBRARY_PATH`
environment variable. These parameters are set to the default values for linux
in the Maven pom.xml as properties, but in order to run problem instances from
compiled code, you need to set the environment variable

```
export LD_LIBRARY_PATH=/usr/local/lib
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
