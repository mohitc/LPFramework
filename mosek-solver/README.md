# Mosek Solver

The Mosek solver converts the programmed model to be solved
using [Mosek](https://www.mosek.com/). Mosek provides a
[Java API](https://docs.mosek.com/11.0/javaapi/index.html)
and the solver converts an
`LPModel` instance into an equivalent problem instance in the Mosek Java API.

## Installation Instructions

Installation instructions for Mosek can be found on the
Mosek [website](https://www.mosek.com/downloads/). Mosek is also installed with
the [docker image](../dev-env-fedora/README.md) used as the development
environment. Mosek provides instructions for setting up licenses and these need
to be configured in the docker image in order to run problem instances using
Mosek. Typically, this can be achieved by using the `--mount` operations for a
docker image to put the Mosek license in the desired location in the development
environment and ues that to run the problem instances.

For example, if you want to mount the license file under the root user in the
docker container, you can update the commands to run the container to

```shell
$ docker run \
  --mount type=bind,src=./,dst=/root/projects/LPFramework \
  --mount type=bind,src={path-to-mosek-license-folder},dst=/root/mosek \
  -it ghcr.io/mohitc/lpframework/dev-env-fedora:main \
  /bin/bash

```

## Running Problem instances

The Mosek solver needs the references to the Mosek libraries. In order to run
the integration tests and other problem instances, the `LD_LIBRARY_PATH` needs
to be configured to point to these libraries which are located under the
`tools/platform/{platform_type}/bin` in your installation.

## Running Integration Tests

Sample problem instances based on the [
`lp-solver-sample`](../lp-solver-sample/README.md) can be run as integration
tests, and are disabled by default. In order to enable the tests, go to the
parent pom and set the property:

```xml

<mosek.skiptests>false</mosek.skiptests>
```

and configure the properties `mosek-root-path`, and if required change the
platform in the `ld-library-path` property in the POM to point to the correct
mosek installation and platform name. Once done, run the target

```shell
$ mvn clean verify
```

to run the problem instances.