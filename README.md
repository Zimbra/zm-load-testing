# Overview

This contains zimbra jmeter load tests.

# Directories

* tests

  Directory of jmeter tests. See tests [README.md](tests/README.md) for more details.

* src

  Source code for Zimbra JMeter Java Library. See src [README.md](src/README.md) for more details.

* jar

  Pre-compiled Zimbra JMeter Java Library jar file.

# Quick Start

Assumes you have [jmeter 3.0](https://archive.apache.org/dist/jmeter/binaries/) installed.

```
# grab a copy of this repo
$ get clone https://github.com/Zimbra/zm-load-testing.git
$ cd zm-load-testing

# create a users.csv file with accounts that can be used for testing zimbra
# <user>,<password>
$ vi /tmp/users.csv
user1,userpass
...

# create zimbra environment specific property file for desired test (example tests/fixed/imapbenchmarking)
$ cp tests/fixed/imapbenchmarking/env.prop /tmp/imapbm.prop
$ vi /tmp/imapbm.prop
modify file appropriately for the zimbra environment you plan to test
update the users.csv file to the csv file created

# run the test
$ jmeter -n -q /tmp/imapbm.prop -q tests/fixed/imapbenchmarking/load.prop -q tests/fixed/imapbenchmarking/imapbenchmarking.jmx
```

# Notes

1. Apache Common Net library has a bug with proxy support and will not send DNS lookups through the proxy so host names fail but IP addresses work. See: https://issues.apache.org/jira/browse/NET-468 https://issues.apache.org/jira/browse/NET-650
