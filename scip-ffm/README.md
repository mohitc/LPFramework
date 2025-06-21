# SCIP Native Bindings via the Foreign Function and Memory Interface

This module generates the Java bindings to the Native C interface for
the [SCIP Solver](https://www.scipopt.org)
using the Java FFM interface. The bindings are auto-generated
using [JExtract](https://jdk.java.net/jextract/) and the rest of the module
provides a simple wrapper around the bindings including

* Enums for native variables
* Wrapper methods around native library calls for Java Native types to pointer
  conversions

## Pre-requisites

In order to build the bindings from the native C interfaces, the implementation
requires:

* JExtract to generate the Java to C binding
  code. [Reference](../README.md#step-1-install-and-configure-jextract)
* SCIP installation

These dependencies are available and tested within the docker container
`dev-env-fedora` provided with the project. In you just want to build the SCIP
bindings, use the [instructions](../dev-env-fedora/README.md) to download/build
the docker container and use it to have an environment with the necessary
pre-requisites.

### Installing SCIP

SCIP is available under the Apache 2.0 license at https://www.scipopt.org. The
installation uses the `cmake` build system to build the solver, and instructions
for installing the package are available with the project. The installation also
assumes the availability of some other packages which are indicated during the
installation process.

For a basic installation in Fedora linux, follow the steps below:

* Install dependencies

```shell
$ dnf install gcc \
    g++ \
    cmake \
    coin-or-Ipopt-devel \
    gmp-devel \
    zlib-ng-devel \
    zlib-ng-compat-devel \
    readline-devel \
    boost-devel \
    tbb-devel
```

* Download and unpack SCIP Optimization Suite

```shell
$ wget https://www.scipopt.org/download/release/scipoptsuite-9.2.2.tgz -P /tmp && \
    tar -zxvf /tmp/scipoptsuite-9.2.2.tgz -C /opt && \
    ln -s /opt/scipoptsuite-9.2.2 /opt/scipopt
```

* Build and install the SCIP Optimization Suite

```shell
$ mkdir /opt/scipopt/build && \
    cd /opt/scipopt/build && \
    cmake .. && \
    make && \
    make install
```

This will install the HiGHS solver at `/usr/local/lib64` and the headers at '
/usr/local/include' which is the default location assumed by the project. If you
choose to change this location, update the property `scip-header.path` and
`scip-library.path` in the POM to the appropriate location.

## Building the module

Once the `scip-header.path`  and `scip-library.path` are configured correctly,
you can just install the module using the command

```
$ mvn clean install
```

This module can be used independently of the general LPSolver framework to call
the SCIP solver from Java, but is primarily maintained as a dependency to be
used by the [scip-solver](../scip-solver/README.md) instance.
