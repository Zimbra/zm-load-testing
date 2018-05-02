# SMTP Benchmarking Test

Sends a messages from book.csv file.

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
