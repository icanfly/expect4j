# Introduction #

The build is primarily a maven build. The one big caveat is that tcljava and jsch are out of date in the maven repository, and needs to be added to your local repository. After that, the build is a standard 'mvn compile' and 'mvn test'

# Local Repository #

The specific commands have been provided in the source, in a file called install.jacl. I just run each line one-by-one.

### tcljava ###

mvn install:install-file -Dfile=lib\tcljava1.4.0\jacl.jar -DgroupId=jacl -DartifactId=jacl -Dversion=1.4.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=lib\tcljava1.4.0\tcljava.jar -DgroupId=jacl -DartifactId=tcljava -Dversion=1.4.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=lib\tcljava1.4.0\janino.jar -DgroupId=jacl -DartifactId=janino -Dversion=1.4.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=lib\tcljava1.4.0\itcl.jar -DgroupId=jacl -DartifactId=itcl -Dversion=1.4.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=lib\tcljava1.4.0\tjc.jar -DgroupId=jacl -DartifactId=tjc -Dversion=1.4.0 -Dpackaging=jar -DgeneratePom=true

### jsch ###

mvn install:install-file -Dfile=lib\jsch-0.1.32.jar -DgroupId=jsch -DartifactId=jsch -Dversion=0.1.32 -Dpackaging=jar -DgeneratePom=true

# Build #

  * mvn compile
  * mvn package

# Test #

There is a timing nature to Expect, and hence the tests have hard coded delays to simulate the passing of time. This slows down the tests a little.

  * mvn test

To skip tests during a build:

  * mvn -Dmaven.skip.tests package