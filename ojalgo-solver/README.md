# ojAlgo Solver

The [ojAlgo solver](https://www.ojalgo.org) is a native Java solver allowing problem instances to be solved purely in the JVM. The `ojalgo-solver` module
implements the [`lp-solver`](../lp-solver/README.md)  and converts and solves an `LPModel` instance using the ojalgo solver.

## Installation Instructions

As ojAlgo runs primarily in the JVM, no additional installation is required to support this solver.

## Running Problem instances

ojAlgo problem instances require no additional installation / configuration, and can be run as is.

## Running Integration Tests

Sample problem instances can be found
as [integration tests](./src/integration-test/kotlin) folder and use the
instances in the [`lp-solver-sample`](../lp-solver-sample/README.md) module. In
order to run these instances, just use the command

```shell
mvn clean verify
```

to run the problem instances.
