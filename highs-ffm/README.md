# HiGHS Native Bindings via the Foreign Function and Memory Interface

This module generates the Java bindings to the Native C interface for
the [HiGHS Solver](https://highs.dev)
using the Java FFM interface. The bindings are auto-generated
using [JExtract](https://jdk.java.net/jextract/) and the rest of the module
provides a simple wrapper around the bindings including

* Enums for native variables
* Wrapper methods around native library calls for Java Native types to pointer
  conversions
* Some helpers around the native functions for easier data extraction.

## Pre-requisites

In order to build the bindings from the native C interfaces, the implementation
requires:

* JExtract to generate the Java to C binding
  code. [Reference](../README.md#step-1-install-and-configure-jextract)
* HiGHS installation

These dependencies are available and tested within the docker container
`dev-env-fedora` provided with the project. In you just want to build the HiGHS
bindings, use the [instructions](../dev-env-fedora/README.md) to download/build
the docker container and use it to have an environment with the necessary
pre-requisites.

### Installing HiGHS

HiGHS is an open source project hosted on Github
at https://github.com/ERGO-Code/HiGHS. The installation uses the `cmake` build
system to build the solver, and instructions for installing the package are
available with the project. For a basic installation in linux, follow the steps
below:

```shell
$ dnf install gcc g++ cmake
$ cd /opt/ && \
    git clone --branch v1.11.0 --single-branch https://github.com/ERGO-Code/HiGHS.git && \
    cd /opt/HiGHS && \
    cmake -S . -B build && \
    cmake --build build
```

This will install the HiGHS solver at `/opt/HiGHS` which is the default location
assumed by the project. If you choose to change this location, update the
property `highs-root.path` in the POM to the appropriate location.

## Building the module

Once the `highs-root.path` is configured correctly, you can just install the
module using the command

```
$ mvn clean install
```

This module can be used independently of the general LPSolver framework to call
HiGHS from Java, but is primarily maintained as a dependency to be used by
the [highs-solver](../highs-solver/README.md) instance.
