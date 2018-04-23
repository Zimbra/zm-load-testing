# Overview

This contains zimbra jmeter load tests.

# Directories

* src

  Source code for Zimbra Java Library.

* jar

  Pre-compiled Zimbra Java Library jar file.

# Notes

1. Apache Common Net library has a bug with proxy support and will not send DNS lookups through the proxy so host names fail but IP addresses work. See: https://issues.apache.org/jira/browse/NET-468 https://issues.apache.org/jira/browse/NET-650
