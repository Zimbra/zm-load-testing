# Overview

This contains zimbra jmeter load tests.

# Directories

* tests

  Directory of jmeter tests. See tests [README.md](tests/README.md) for more details.

* src

  Source code for Zimbra JMeter Java Library. See src [README.md](src/README.md) for more details.

* jar

  Pre-compiled Zimbra JMeter Java Library jar file.

# Notes

1. Apache Common Net library has a bug with proxy support and will not send DNS lookups through the proxy so host names fail but IP addresses work. See: https://issues.apache.org/jira/browse/NET-468 https://issues.apache.org/jira/browse/NET-650
