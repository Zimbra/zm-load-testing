# Tests

This directory containts all the JMeter tests that have been created for testing Zimbra performance.

The tests are broken up into two groups

* fixed

The fixed tests can be run against different environments and with different loads but what the test does is fixed inside the jmx file.

A typical execution of a fixed test is of the form:

```
jmeter -q env.prop -q load.prop -t test.jmx
```

* generic 

The generic tests can be run against different environments, with different loads and using a profile can performe different sequences of protocol requests.

A typical execution of a generic test is of the form:

```
jmeter -q env.prop -q load.prop -q profile.prop -t test.jmx
```

For more details and example of running generic tests see generic [README.md](generic/README.md).
