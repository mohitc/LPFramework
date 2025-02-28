# GLPK Native Bindings via the Foreign Function and Memory Interface

This module generates the Java bindings to the Native C interface for the GLPK library using the Java FFM interface.
The bindings are auto-generated using [JExtract](https://jdk.java.net/jextract/) and the rest of the module provides
a simple wrapper around the bindings including 

  * Enums for native variables 
  * Wrapper methods around native library calls for Java Native types to pointer conversions 
  * A basic data class GlpIocp to configure the GLPK instance with sensible defaults

## Pre-requisites 

In order to build the bindings from the native GLPK C interfaces, the implementation requires: 
  * JExtract to generate the Java to C binding code. [Reference](../README.md#install-and-configure-jextract)
  * GLPK Native library installation

### Installing GLPK
GLPK is available at [gnu.org](https://www.gnu.org/software/glpk/glpk.html) and this implementation has been tested 
with the latest available version 5.0. The GLPK package can be installed via the GNU `make` build system, and 
instructions for installing the package are available. For a basic installation in linux, follow the steps below: 
```shell
$ wget https://ftp.gnu.org/gnu/glpk/glpk-5.0.tar.gz
$ tar -xvf glpk-5.0.tar.gz
$ cd glpk-5.0
$ ./configure
$ make
$ make install
```

## Building the module

Apart from the references mentioned above, the module requires a reference to the GLPK C header file `glpk.h`
which is included with the GLPK installation. 

For standard Linux installations, this should default to 
`/usr/local/include` but if the files are not found there, the compilation will error out. In case it cannot find the 
header paths, search for the file `glpk.h` and then update the environment variable
```xml
    <glpkc-header.path>...</glpkc-header.path>
```
in the module's `pom.xml` file and update the path there accordingly. 

In order to build and install the generated library use the command
```
$ mvn clean install
```
Once this module is installed, proceed with the compilation of the `glpk-solver` [module](../glpk-solver/README.md).
