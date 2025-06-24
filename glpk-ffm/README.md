# GLPK Native Bindings via the Foreign Function and Memory Interface

This module generates the Java bindings to the Native C interface for the GLPK
library using the Java FFM interface. The bindings are auto-generated
using [JExtract](https://jdk.java.net/jextract/) and the rest of the module
provides a simple wrapper around the bindings including

* Enums for native variables
* Wrapper methods around native library calls for Java Native types to pointer
  conversions
* A basic data class GlpIocp to configure the GLPK instance with sensible
  defaults

## Pre-requisites

In order to build the bindings from the native GLPK C interfaces, the
implementation requires:

* JExtract to generate the Java to C binding
  code. [Reference](../README.md#step-1-install-and-configure-jextract)
* GLPK Native library installation

These dependencies are available and tested within the docker container
`dev-env-fedora` provided with the project. In you just want to build the GLPK
bindings, use the [instructions](../dev-env-fedora/README.md) to download/build
the docker container and use it to have an environment with the necessary
pre-requisites.

### Installing GLPK

GLPK is available at [gnu.org](https://www.gnu.org/software/glpk/glpk.html) and
this implementation has been tested with the latest available version 5.0. The
GLPK package can be installed via the GNU `make` build system, and instructions
for installing the package are available. For a basic installation in linux,
follow the steps below:

```shell
$ wget https://ftp.gnu.org/gnu/glpk/glpk-5.0.tar.gz
$ tar -xvf glpk-5.0.tar.gz
$ cd glpk-5.0
$ ./configure
$ make
$ make install
```

Note that GLPK is built using ANSI-C standard, and may not compile with later C
language specifications. In order to use the ANSI-C specifications, configure
the environment flag

```shell
export CFLAGS="-ansi"
```

before running the configure command in the instructions above.

## Building the module

Apart from the references mentioned above, the module requires a reference to
the GLPK C header file `glpk.h` which is included with the GLPK installation.

For standard Linux installations, this should default to
`/usr/local/include` but if the files are not found there, the compilation will
error out. In case it cannot find the header paths, search for the file `glpk.h`
and then update the property

```xml

<glpkc-header.path>...</glpkc-header.path>
```

in the module's `pom.xml` file and update the path there accordingly.

In order to build and install the generated library use the command

```
$ mvn clean install
```

This module can be used independently of the general LPSolver framework to call
GLPK from Java, but is primarily maintained as a dependency to be used by
the [glpk-solver](../glpk-solver/README.md) instance.
