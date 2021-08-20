# Overview

This contains zimbra jmeter load tests.

# Directories

* config

  Required configuration files for ant to run against zimbra environment.

* tests

  Directory of jmeter tests. See tests [README.md](tests/README.md) for more details.

* src

  Source code for Zimbra JMeter Java Library. See src [README.md](src/README.md) for more details.

* data

  Contain jmx file for data generation. Take input from config/users.csv to create accounts.

* reportGen

  Contain scripts used to execute test and generate graphs and other reports using the execution results.

* result

  Contain reports when we execute test using loadtest.sh

# Quick Start

## Ant

### Assumptions

1. Java Developer Kit (JDK) 7 or higher installed.
2. Ant 1.9 or higher installed.
3. [jmeter 3.0](https://archive.apache.org/dist/jmeter/binaries/) installed at /opt/apache-jmeter-3.0 if not modify build.xml jmeter.home appropriately.
4. config/env.prop is configured for the Zimbra environment to be tested.
5. admin account exists and has password of test123 in the Zimbra environment (modify config/users.csv if other account(s) desired).
6. See src [README.md](src/README.md) for addtional requirements to generate Zimbra JMeter Java Library.

### Execution

```
$ git clone https://github.com/Zimbra/zm-load-testing.git
$ cd zm-load-testing
$ ant
```

Data Generation

```
$ ant dataGen
```

To run a specific test use the path name of the test seperated by hyphen example:

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

Execution with reports generation

```
Ssh key addition of client to the server root user authorized_keys(.ssh/authorised_keys file).
create required jars

$ ant jar
$ ant src
$ sh reportGen/scripts/loadtest.sh -t <test name> -w < workspace path>
  test name: use the path name of the test seperated by hyphen (eg: fixed-ephemeral)
```

## Manual

### Assumptions

1. Java Developer Kit (JDK) 7 or higher installed.
2. [jmeter 3.3](https://archive.apache.org/dist/jmeter/binaries/) installed.

### Execution

1. grab a copy of this repo

   ```
   $ git clone https://github.com/Zimbra/zm-load-testing.git
   $ cd zm-load-testing
   ```

2. create a users.csv file with accounts that can be used for testing zimbra
   see specific test details for required contents for this example:
   &lt;user&gt;,&lt;password&gt;

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
