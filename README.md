mongodb_M101J_java8
===================

The Java program content of MongoDB University course M101J, "MongoDB for Java Developers", is presented here 
with code updated to utilize the Spark 2.0.0 web server, the Freemarker template engine, and the Lambda expressions
capability of Java 8.

Course content is presented as Gradle projects. In the actual course, Maven is used as the build tool instead of
Gradle. 

The repository is divided into a series of folders. Each folder represents a specific week of course content in
the M101J course, starting with Week 1 and ending with Week 7.

## System Requirements ##

You must have a Java 8 Development Kit installed. It must be pointed to in your JAVA_HOME environment variable.

Java 8 can be downloaded from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

You must have Gradle installed. Gradle's bin directory must be pointed to in your GRADLE_HOME environment variable.

Gradle can be found at http://www.gradle.org/downloads.

### Importing into IntelliJ IDEA 14 ###

Gradle projects are supported in the the "IntelliJ IDEA" integrated development environment tool.
 
If you are on OS X 10.10, Start IntelliJ from a terminal window so that it can access your environment variables. 
Follow the normal procedure for importing a project. For example, navigate to the "week4" directory and IntelliJ
will automatically detect the build.gradle file. Note that IntelliJ does not seem to recognize the 
GRADLE_HOME environment variable. It will expect you to point it to the root directory where Gradle's binary 
files are kept. 

#### Project Contributors ####

Ted Chen, creator of repository  
Robert L. Cochran provided documentation updates. <r2cochran2@gmail.com>


