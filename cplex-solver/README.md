# CPLEX Solver

The CPLEX solver converts the programmed model to be solved using
the [IBM ILOG CPLEX](https://www.ibm.com/products/ilog-cplex-optimization-studio)
solver. The installation of the tool comes with a packaged jar file `cplex.jar` which is usually located under
the `CPLEX_INSTALL_FOLDER/cplex/lib/cplex.jar`.

## Installation Instructions

CPLEX does not maintain a repository for the jar files, and as a result, the module is not included in the default
compilation. In order to compile the `cplex-solver` module, please follow the following steps.

- Navigate to the folder with the file `cplex.jar` and run the following command to install the jar as a maven artifact
  in the local repository

```
mvn install:install-file -Dfile=cplex.jar -DgroupId=cplex -DartifactId=cplex -Dversion=1.0 -Dpackaging=jar
```

- Navigate to the `pom.xml` file in the base project and uncomment the module dependency

```    
<module>cplex-solver</module>
```

- Run the command `mvn clean install` after which the `cplex-solver` can be instantiated as a dependency in other
  projects:

```xml

<dependency>
  <groupId>com.lpapi</groupId>
  <artifactId>cplex-solver</artifactId>
  <version>${lpapi.version}</version>
</dependency>
```

## Running Problem instances

The CPLEX solver needs the references to the CPLEX jni references. In order to do so, we need to include the reference
to the CPLEX JNI libraries as a VM argument. The CPLEX jni libraries are usually included at the same location as the
CPLEX binaries, and can be found, for example in linux in `CPLEX_INSTALL_FOLDER/cplex/bin/[system architecture]`. In
order to run the solver, please include the VM argument `-Djava.library.path=[path_to_cplex_jni_libraries]`. For
example:

```
-Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community129/cplex/bin/x86-64_linux
```

## Running Test Problem instances

Sample problem instances can be found as integration tests in the cplex-solver project, and are disabled by default. In
order to enable the tests, go to the parent pom and set the property:

```xml    
<cplex.skiptests>false</cplex.skiptests>
```

In order to include the JVM arguments when running the tests, either include them as an environment variable, e.g.

```
MAVEN_OPTS=" -Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community129/cplex/bin/x86-64_linux"
export MAVEN_OPTS
```

After the environment variables are set, run the target:

```
mvn clean verify
```

Another option is to include the path to the library directly in the command line:

```
mvn clean verify -DargLine="-Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community129/cplex/bin/x86-64_linux"
```