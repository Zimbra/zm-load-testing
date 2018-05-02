# ant build

requires: java 7, ant 1.9, junit, and hamcrest

centos: yum install java ant ant-contrib ant-junit

```
$ ant
```

all generated items are in build directory.

* jar = zjmeter jar file
* doc = zjmeter documentation
* classes = zjmeter classes
* test = zjmeter test classes

```
$ ant clean 
```

remove build directory.

# manual build
## build

```
shopt -s globstar
javac -cp java java/**/*.java
```

## command line testing

```
java -cp java com.zimbra.jmeter.test command SOAP properties/sequence.prop
java -cp java com.zimbra.jmeter.test command SOAP properties/test.prop
```

## jar

```
cd java; jar -cf ../zjmeter.jar .
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
