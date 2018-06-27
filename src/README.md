# ant build

requires: java 8, jmeter 4.0, ant 1.9, junit, and hamcrest

centos: yum install java ant ant-contrib ant-junit

```
$ ant
```

all generated items are in build directory.

* jar = jar files
* doc = documentation
* classes = classes
* test = test classes

For eas testing the zimbraext.jar file must be installed to the jmeter lib/ext directory. This build assumes jmeter is available at /opt/apache-jemeter-4.0 if not then set jmeter.home to the proper location. If you have write access tot he jmeter location you can use:

```
$ ant install
```

To copy the zimbraext.jar to the jmeter lib/ext directory.

```
$ ant clean 
```

will remove build directory.

# manual build
## build

```
shopt -s globstar
javac -cp java:/opt/apache-jmeter-4.0/lib/*:/opt/apache-jmeter-4.0/lib/ext/* java/**/*.java
```

## command line testing

```
java -cp java com.zimbra.jmeter.test command SOAP properties/sequence.prop
java -cp java com.zimbra.jmeter.test command SOAP properties/test.prop
```

## jar

```
cd java; jar -cf ../zjmeter.jar com/zimbra/jmeter/*.class
cd java; jar -cf ../zimbraext.jar com/zimbra/jmeter/ext/*.class
```

## jar command line testing

```
java -cp zjmeter.jar com.zimbra.jmeter.test command SOAP properties/sequence.prop
java -cp zjmeter.jar com.zimbra.jmeter.test command SOAP properties/percent.prop
```

## class cleanup

```
shopt -s globstar
rm java/**/*.class
```

## junit test

```
mkdir lib
cd lib
wget http://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar -O junit-4.12.jar
wget http://search.maven.org/remotecontent?filepath=org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar -O hamcrest-core-1.3.jar
shopt -s globstar
javac -cp java:java-test:lib/junit-4.12.jar:lib/hamcrest-core-1.3.jar java-test/**/*.java
java -cp java:java-test:lib/junit-4.12.jar:lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore com.zimbra.jmeter.TestCommand
```

## test class cleanup

```
shopt -s globstar
rm java-test/**/*.class
```

## javadoc 

```
shopt -s globstar
javadoc -d ../docs/jdoc java/**/*.java
```
