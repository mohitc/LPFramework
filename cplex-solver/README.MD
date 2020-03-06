#CPLEX Solver 

The CPLEX solver converts the programmed model to be solved using the [IBM ILOG CPLEX](https://www.ibm.com/products/ilog-cplex-optimization-studio) 
solver. The installation of the tool comes with a packaged jar file <code>cplex.jar</code> which is usually located under the
<code>INSTALL_FOLDER/cplex/lib/cplex.jar</code>. 

##Installation Instructions
CPLEX does not maintain a repository for the jar files, and as a result, the module is not included in the default compilation. 
In order to enable the compilation of the cplex-module, please follow the following steps. 

  - Navigate to the folder with the file <code>cplex.jar</code> and run the following command to install the jar as a maven artifact in 
  the local repository 
  ```
mvn install:install-file -Dfile=cplex.jar -DgroupId=cplex -DartifactId=cplex -Dversion=1.0 -Dpackaging=jar
```

  - Navigate to the <code>pom.xml</code> file in the base project and uncomment the module dependency 
  ```    
<module>cplex-solver</module>
```

## Running Problem instances 
The CPLEX solver needs the references to the CPLEX jni references. In order to do so, we need to include the reference to the 
CPLEX JNI libraries as a VM argument. The CPLEX jni libraries are usually included at the same location as the CPLEX binaries, 
and can be found, for example in linux in <code>[CPLEX Install Folder]/cplex/bin/[system architecture]</code>. In order to 
run the solver, please include the VM argument <code>-Djava.library.path=[Path to Cplex jni libraries]</code>, for example 
```
-Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community129/cplex/bin/x86-64_linux
```

## Running Test Problem instances 
Sample problem instances can be found as tests in the cplex-solver project, and are disabled by default. In order to enable 
the tests, go to the parent pom and set the property 
```xml    
<cplex.skiptests>true</cplex.skiptests>
```

In order to include the JVM arguments when running the tests, either include them as an environment variable, e.g. 
```
MAVEN_OPTS=" -Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community129/cplex/bin/x86-64_linux"
export MAVEN_OPTS
```
and then run the target 
```
mvn clean compile test
```
or include them directly in the command line 
```
mvn clean compile test -DargLine="-Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio_Community129/cplex/bin/x86-64_linux"
```