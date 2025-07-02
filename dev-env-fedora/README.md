# Building and Running the LPFramework project

The LPFramework project uses a number of open-source solvers, and as part of the
build process, generates Java/Kotlin bindings to the C interfaces for these
projects. In order to build the project consistently, this project provides a
docker development environment that can be used to provide a consistent
environment for building the LPFramework project. Once built, the libraries can
be used across various development environments.

## Using the Docker instances from Github

The projects automation build and publish the docker image to the Github
container registry periodically. In order to use the pre-built image, navigate
to the root folder of the project and run the following command

```shell
$ docker run \
  --mount type=bind,src=.,dst=/root/projects/LPFramework \
  -it ghcr.io/mohitc/lpframework/dev-env-fedora:master \
  /bin/bash
```

This command basically binds the local installation folder for the project to
`/root/projects/LPFramework` in the docker container, and provides you a bash
instance in the container. In order to compile project, just use the command

```shell
$ cd /root/projects/LPFramework && mvn clean install
```

## Building the Docker container and project locally

The following section outlines the steps to build the docker container and
subsequently the project locally. In order to do so, you should have docker
installed in your development
environment (https://docs.docker.com/engine/install/).

In order to build the docker container locally, use the command line below to
build the docker image defined by the Dockerfile in this folder.

```
$ docker build --tag lp-framework/dev-env-fedora .
```

This image builds on a Fedora instance, and sets up a development environment
that includes:

* Adoptium Temurin JDK 24
* Apache Maven (v 3.9.10)
* JExtract Early Access (v22)
* GLPK v5.0
* SCIP Optimization Suite (v9.2.2)
* HiGHS Solver (v1.11.0)

In order to build and explore the project, you can navigate to the root folder
in this project and use the command line below run the docker image, mount the
project directory under `/root/projects/LPFramework` and access a console in the
container.

```shell
$ docker run \
  --mount type=bind,src=./,dst=/root/projects/LPFramework \
  -it lp-framework/dev-env-fedora \
  /bin/bash
```

Once you have access, navigate to the correct folder in the docker image and
build the project as shown below:

```shell
$ cd /root/projects/LPFramework && mvn clean install
```

