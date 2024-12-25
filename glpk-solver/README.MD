#GLPK Solver 

The GLPK solver converts the programmed model to be solved using the [GNU Linear Programming Kit](https://www.gnu.org/software/glpk/) 
which is an open source alternative for solving linear programming models. GLPK is written in C, and in order to call methods 
from Java/Kotlin, the project uses the [GLPK Java] (http://glpk-java.sourceforge.net/) project 

##Installation Instructions
In order to evaluate models using this solver, we need to install both the GLPK project and the GLPK Java bindings. 

  - Download and install instructions for GLPK can be found [here](https://www.gnu.org/software/glpk/#TOCdownloading). This 
  project was tested with the latest version of GLPK at the moment (4.65)

  - Pre-built windows binaries and Debian/Ubuntu packages for the GLPK Java project are available and instructions for installing 
  the same can be found [here] (http://glpk-java.sourceforge.net/). To build the project from scratch, the sources of the 
  GLPK Java project can be found here (https://sourceforge.net/projects/glpk-java/files/). This project was tested using v1.12.0. 
  Note that while the pom dependency is already available, the GLPK Java installation is needed to generate the call bindings to 
  GLPK. 
  
  - When building with Java11, there is a known issue that is observed while generating the javadocs. 
  [https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8212233](https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8212233)
  In order to bypass this issue, after running configure, navigate to the <code>swig</code> directory and either disable 
  the plugin instances for <code>org.apache.maven.plugins:maven-javadoc-plugin</code> and 
  <code>org.apache.maven.plugins:maven-site-plugin</code> or apply the workarounds as described in the bug report
  
  
## Running Problem instances 
The GLPK solver needs the references to the glpk-java jni references. In order to do so, please include the following 
VM argument <code>-Djava.library.path=/usr/local/lib/jni:/usr/lib/jni</code>
