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

Sample problem instances can be found as integration tests in the gurobi-solver
project, and are disabled by default. In order to enable the tests, go to the
parent pom and set the property:

```xml

<gurobi.skiptests>false</gurobi.skiptests>
```

In order to include the JVM arguments when running the tests, either include
them as an environment variable, e.g.

```
MAVEN_OPTS=" -Djava.library.path=$GUROBI_HOME/jni"
export MAVEN_OPTS
```

and then run the target

```
mvn clean verify
```

or include them directly in the command line

```
mvn clean verify -DargLine="-Djava.library.path={PATH_TO_GUROBI_HOME}/lib"
```