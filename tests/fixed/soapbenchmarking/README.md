# SOAP Benchmarking Test

Login check the inbox periodically until a message is availble then perform a specific set of additional steps.

# Properties

## Environment

|Key         |Value          |Description                                 |
|------------|---------------|--------------------------------------------|
|HTTP.server |host.domain.com|name to use to connect to web server        |
|ZIMBRA.domain |domain.com     |domain to use in e-mail addresses           |
|HTTP.port   |443            |port to use with web server                 |
|ACCOUNTS.csv|users.csv      |csv file of test accounts (user,pass,touser)|
|REQUEST.log |requests.log   |file to log requests to                     |

## Load

|Key                   |Value|Description                                 |
|----------------------|-----|--------------------------------------------|
|LOAD.duration         |60   |test duration in seconds                    |
|LOAD.delay            |0    |test start delay in seconds                 |
|LOAD.users            |1    |concurrent users/threads to run during tests|
|LOAD.rampup           |0    |how long to spend ramping up threads        |
|LOAD.loopcount        |1    |how many times the user/thread repeats      |

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
$ cp tests/fixed/soapbenchmarking/env.prop /tmp/myenv.prop
$ vi /tmp/myenv.prop
modify file appropriately for the zimbra environment you plan to test
update the users.csv file to the csv file created above

# run the predifined load and basic profile without using the GUI
$ jmeter -n -q /tmp/myenv.prop -q tests/fixed/soapbenchmarking/load.prop -t tests/fixed/soapbenchmarking/soapbenchmarking.jmx
```
