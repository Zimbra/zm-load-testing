# SMTP Benchmarking Test

Sends a messages from book.csv file.

# Properties

## Environment

|Key         |Value          |Description                                 |
|------------|---------------|--------------------------------------------|
|SMTP.server |host.domain.com|name to use to connect to web server        |
|SMTP.domain |domain.com     |domain to use in e-mail addresses           |
|SMTP.port   |25             |port to use with web server                 |
|ACCOUNTS.csv|users.csv      |csv file of test accounts (user,pass,touser)|
|MESSAGES.csv|book.csv       |csv file of messages (message)              |
|REQUEST.log |requests.log   |file to log requests to                     |

## Load

|Key                   |Value|Description                                 |
|----------------------|-----|--------------------------------------------|
|LOAD.SMTP.users       |1    |concurrent users/threads to run during tests|
|LOAD.SMTP.rampup      |0    |how long to spend ramping up threads        |
|LOAD.SMTP.loopcount   |1    |how many times the user/thread repeats      |
|LOAD.SMTP.duration    |60   |duration of the test in seconds             |

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
$ cp tests/fixed/smtpbenchmarking/env.prop /tmp/myenv.prop
$ vi /tmp/myenv.prop
modify file appropriately for the zimbra environment you plan to test
update the users.csv file to the csv file created above

# run the predifined load and basic profile without using the GUI
$ jmeter -n -q /tmp/myenv.prop -q tests/fixed/smtpbenchmarking/load.prop -t tests/fixed/smtpbenchmarking/smtpbenchmarking.jmx
```

The message to send are in the book.csv you can add any message you like between quotes. If the message contains quotes they must be escaped using two consecutive quotes.
