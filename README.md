# Overview

This contains zimbra jmeter load tests.

# Directories

* config

  Required configuration files for ant to run against zimbra environment.

* tests

  Directory of jmeter tests. See tests [README.md](tests/README.md) for more details.

* src

  Source code for Zimbra JMeter Java Library. See src [README.md](src/README.md) for more details.

# Quick Start

## Ant

### Assumptions

1. Java Developer Kit (JDK) 7 or higher installed.
2. [jmeter 3.0](https://archive.apache.org/dist/jmeter/binaries/) installed at /opt/apache-jmeter-3.0 if not modify build.xml jmeter.home appropriately.
3. config/env.prop is configured for the Zimbra environment to be tested.
4. user1 account exists and has password of userpass in the Zimbra environment (modify config/users.csv if other account(s) desired).
5. See src [README.md](src/README.md) for addtional requirements to generate Zimbra JMeter Java Library.

### Execution

```
$ get clone https://github.com/Zimbra/zm-load-testing.git
$ cd zm-load-testing
$ ant
```

To run a specific test use that path name of the tests seperated by hypen example:

```
$ ant fixed-ephemeral
```

To specify a particular environment file:

```
$ ant -Denv=config/myenv.prop
```

To generate the Zimbra JMeter Java Library only:

```
$ ant src
```

## Manual

### Assumptions

1. Java Developer Kit (JDK) 7 or higher installed.
2. [jmeter 3.0](https://archive.apache.org/dist/jmeter/binaries/) installed.

### Execution

1. grab a copy of this repo

   ```
$ get clone https://github.com/Zimbra/zm-load-testing.git
$ cd zm-load-testing
   ```

2. create a users.csv file with accounts that can be used for testing zimbra
   see specific test details for required contents for this example:
   <user>,<password>

   ```
$ vi /tmp/users.csv
user1,userpass
...
   ```

3. create zimbra environment specific property file
   example: tests/fixed/imapbenchmarking

   ```
$ cp tests/fixed/imapbenchmarking/env.prop /tmp/imapbm.prop
$ vi /tmp/imapbm.prop
modify file appropriately for the zimbra environment you plan to test
update the users.csv file to the csv file created
   ```

4. run the test

   ```
$ jmeter -n -q /tmp/imapbm.prop -q tests/fixed/imapbenchmarking/load.prop -t tests/fixed/imapbenchmarking/imapbenchmarking.jmx
   ```

# Notes

1. Apache Common Net library has a bug with proxy support and will not send DNS lookups through the proxy so host names fail but IP addresses work. See: https://issues.apache.org/jira/browse/NET-468 https://issues.apache.org/jira/browse/NET-650
