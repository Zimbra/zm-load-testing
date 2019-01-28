# Ephemeral Test

This performs authentications to the web interface followed by a single SOAP request to validate CSRF value returned.

# Properties

## Environment

|Key         |Value          |Description                              |
|------------|---------------|-----------------------------------------|
|WEB.server  |host.domain.com|name to use to connect to web server     |
|WEB.port    |443            |port to use with web server              |
|WEB.protocol|https          |protocol to use with web server          |
|ACCOUNTS.csv|users.csv      |csv file of test accounts (user,password)|
|REQUEST.log |requests.log   |file to log requests to                  |

## Load

|Key                  |Value|Description                                 |
|---------------------|-----|--------------------------------------------|
|LOAD.duration        |130  |test duration in seconds                    |
|LOAD.delay           |0    |test start delay in seconds                 |
|LOAD.users           |1    |concurrent users/threads to run during tests|
|LOAD.userduration    |1    |user login duration in seconds              |
|LOAD.rampup          |0    |how long to spend ramping up threads        |
|LOAD.loopcount       |1    |how many times the user/thread repeats      |

# Example

```
# grab a copy of the tests
$ get clone https://github.com/Zimbra/zm-load-testing.git
$ cd zm-load-testing

# create a user.csv file of accounts that can be used for testing
# <user>,<password>
# add as many users as you want to test with
$ vi /tmp/users.csv
user1,userpass
...

# create zimbra environment specific property file for desired test example zsoap
$ cp tests/fixed/ephemeral/env.prop /tmp/myenv.prop
$ vi /tmp/myenv.prop
modify file appropriately for the zimbra environment you plan to test
update the users.csv file to the csv file created above

# run the predifined load and basic profile without using the GUI
$ jmeter -n -q /tmp/myenv.prop -q tests/fixed/ephemeral/load.prop -t tests/fixed/ephemeral/ephemeral.jmx
```
