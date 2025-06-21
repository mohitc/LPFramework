# Gurobi Solver

The Gurobi solver converts the programmed model to be solved
using [Gurobi](https://www.gurobi.com/). Gurobi provides a
[Java API](https://www.gurobi.com/documentation/9.1/refman/java_api_overview.html#sec:Java)
and the solver converts an
`LPModel` instance into an equivalent problem instance over the Gurobi Java API.

## Installation Instructions

Installation instructions for Gurobi can be found on the
Gurobi [website](https://www.gurobi.com/documentation/9.1/quickstart_mac/software_installation_guid.html).

## Running Problem instances

The Gurobi solver needs the references to the Gurobi jni references. As part of
most installations, Gurobi sets the
`GUROBI_HOME` environment variable, and the JVM needs pointers to
`${GUROBI_HOME}/lib` in the `LD_LIBRARY_PATH` to successfully solve a model
using Gurobi. These can be included as a VM argument
`-Djava.library.path={PATH_TO_GUROBI_HOME}/lib` when running a program using the
solver.

## Running Integration Tests

Sample problem instances based on the [
`lp-solver-sample`](../lp-solver-sample/README.md) can be run as integration
tests, and are disabled by default. In order to enable the tests, go to the
parent pom and set the property:

```xml

<gurobi.skiptests>false</gurobi.skiptests>
```

and configure the properties `gurobi-home`in the POM to point to the correct
gurobi home location. Once done, run the target

```
mvn clean verify
```

to run the problem instances.