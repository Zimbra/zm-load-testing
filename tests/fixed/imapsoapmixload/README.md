# IMAP/SOAP Benchmarking Combined Load Test

This combines the imapbenchmarking and soapbenchmarking into one test.

# Properties

See imapbenchmarking [README.md](../imapbenchmarking/README.md) and soapbenchmarking [README.md](../soapbenchmarking/README.md) for details.

# Example

```
# grab a copy of the tests
$ get clone https://github.com/Zimbra/zm-load-testing.git 
$ cd zm-load-testing

# create a user.csv file of accounts that can be used for testing
# <user>,<password>,<touser>
# add as many users as you want to test with
$ vi /tmp/users.csv
user1,userpass,user2
...

# create zimbra environment specific property file for desired test example zsoap
$ cp tests/fixed/imapsoapmixload/env.prop /tmp/myenv.prop
$ vi /tmp/myenv.prop
modify file appropriately for the zimbra environment you plan to test
update the users.csv file to the csv file created above

# run the predifined load and basic profile without using the GUI
$ jmeter -n -q /tmp/myenv.prop -q tests/fixed/imapsoapmixload/load.prop -t tests/fixed/imapsoapmixload/imapsoapmixload.jmx
```
