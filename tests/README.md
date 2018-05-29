# Tests

This directory containts all the JMeter tests that have been created for testing Zimbra performance.

The tests are broken up into two groups

* fixed
  
  The fixed tests can be run against different environments and with different loads but what the test does is fixed inside the jmx file.
  
  A typical execution of a fixed test is of the form:
  
  ```
  jmeter -q env.prop -q load.prop -t test.jmx
  ```
  
  For more details and example of running fixed tests see fixed [README.md](fixed/README.md).

* generic 
  
  The generic tests can be run against different environments, with different loads, and using a profile can perform different sequences of protocol commands.
  
  All the generic tests depend on the [Zimbra JMeter Java Library](../src/README.md) to generate the sequence of commands from the profile property file.
  
  A typical execution of a generic test is of the form:
  
  ```
  jmeter -q env.prop -q load.prop -q profile.prop -t test.jmx
  ```
  
  For more details and example of running generic tests see generic [README.md](generic/README.md).

# Scaling

Generally most of theses tests will execute up to about 500 users. However to go beyond that may require adjusting the amount of memory allocated to jmeter. Adjusting the memory size can usually get you up to about 1000 users. However going beyond may result in dramatically increased run times.  Running multiple instanses of jmeter to go beyond 1000 users was found to maintain reasonable runtimes.

With the above in mind although "mix" tests are available those tests are only usefull for very low user numbers and often times it is simpler to just run multiple instances of jmeter each running different protocols. Suppose you want 2000 imap users, 1000 smtp users, and 5000 zsoap users this would generally be done with:

* 2 instances of jmeter imap 1000 users

* 1 instance of jmeter smtp 1000 users

* 5 instances of jmeter zsoap 1000 users

Start all 8 of the above instances at the same time against the same environment.
