# CPLEX Solver

The CPLEX solver converts the programmed model to be solved using
the [IBM ILOG CPLEX](https://www.ibm.com/products/ilog-cplex-optimization-studio)
solver.

## Installation Instructions

CPLEX does not maintain a repository for the jar files, and as a result, the
module is not included in the default compilation. This module can therefore be
built in two separate ways:

### Option 1: Referencing CPLEX jar from a private Maven Repository

If your institution maintains a version of the CPLEX jar in a private maven
repository, you can include a small repository bloc like

```xml

<repositories>
  <repository>
    <id>cplex-repo</id>
    <url>{URL-TO-Maven-Repo}</url>
  </repository>
</repositories>
```

in the [module POM](./pom.xml).

### Option 2: Using the cplex.jar from your local installation

If you do not have access to a maven repository that has CPLEX available, you
can install a local `cplex.jar` file available with your CPLEX installation into
the local maven repository. To do so, navigate to the folder with the file
`cplex.jar` and run the following command to install the jar as a maven artifact
in the local repository

```
mvn install:install-file -Dfile=cplex.jar -DgroupId=cplex -DartifactId=cplex -Dversion=1.0 -Dpackaging=jar
```

After that, you can use the CPLEX profile in project to compile the module. For
example, in order to compile and install the module, you can navigate to the
root project director and run the command

```shell
$ mvn -P cplex clean install
```

## Using the CPLEX solver module

In order to solve problem instances using the CPLEX solver, just include the
following dependency in the project, and the framework will automatically use
the CPLEX libraries to solve the model.

```xml

<dependency>
  <groupId>io.github.mohitc</groupId>
  <artifactId>cplex-solver</artifactId>
  <version>${version}</version>
</dependency>
```

## Running Problem instances

The CPLEX solver needs the references to the CPLEX jni references. In order to
do so, we need to include the reference to the CPLEX JNI libraries as a VM
argument. The CPLEX jni libraries are usually included at the same location as
the CPLEX binaries, and can be found, for example in linux in
`CPLEX_INSTALL_FOLDER/cplex/bin/[system architecture]`. In order to run the
solver, please include the VM argument
`-Djava.library.path=[path_to_cplex_jni_libraries]`. For example, include the
following parameter in your command line:

```
-Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community129/cplex/bin/x86-64_linux
```

## Running Test Problem instances

Sample problem instances are taken from the `lp-solver-sample` package and
included as integration tests in the `cplex-solver` module. In order to run
these test instances, you need to:

1. Enable the CPLEX integration tests in the parent pom by setting the parameter
   `cplex.skiptests` to false (defaults to true).

```xml

<cplex.skiptests>false</cplex.skiptests>
```

1. Navigate to the properties in the POM and update the `cplex-root.path` and
   1ld-library-path` parameters based on the CPLEX installation location and the
   architecture/OS that you are running the instance on.

After the environment variables are set, run the target:

```
mvn clean verify
```
