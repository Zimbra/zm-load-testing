# Generic

These tests provide generic Zimbra supported protocol performance testing.

The following protocols currently have a basic level of support:

* smtp

* lmtp

* imap

* pop

* zsoap

Any of these tests can be used following this basic outline of steps:

```
# grab a copy of the tests
$ get clone https://github.com/Zimbra/zm-load-testing.git 
$ cd zm-load-testing
# create zimbra environment specific property file for desired test example zsoap
$ cp tests/generic/zsoap/env.prop /tmp/myenv.prop
$ vi /tmp/myenv.prop
modify file appropriately for the zimbra envirnment you plan to test
# run the predifined load and basic test without using the GUI
# note: some property files use relative paths that assume jmeter is run from the repo's top directory
$ jmeter -n -q /tmp/myenv.prop -q test/generic/zsoap/load.prop -q test/generic/zsoap/profile/basic.prop -t test/generic/zsoap/zsoap.jmx
```
The default load.prop file for all the tests is a single user that runs the specified profile once then exits. Just as you created a custom env.prop you can copy the load.prop file to adjust load as desired. The same idea also works for creating custom profiles for these generic tests.

The generic tests ideally would support all the protocol supported commands however at this time the jmx files only support a subset of any particular protocols commands. The profile allows you to define any sequence of the available commands to execute.
