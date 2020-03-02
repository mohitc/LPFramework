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

The project should 