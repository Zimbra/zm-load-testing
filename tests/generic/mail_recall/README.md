
# Mail Recall

This test provides generic Zimbra supported protocol performance testing for Mail Recall feature. It supports mail recall for messages sent to 1, 10, 20 and 100 recipients.



# Properties

## Environment

Protocol Support

|Test Type |Test          |Protocol|
|----------|--------------|--------|
|Performance|Mail Recall  |HTTPS   |

The environment file defines how to access the environment specific information.

|Key                    |Value          |Description                 |
|-----------------------|---------------|----------------------------|
|HTTP.server            |server.com     |Server Name   			     |
|HTTP.admin.pass        |Password       |Password      			     |
|ACCOUNTS.csv           |users.csv      |csv file of test accounts   |
|ACCOUNTS_1.csv         |users_10.csv   |csv file of test accounts   |
|ACCOUNTS_2.csv         |users_20.csv   |csv file of test accounts   |
|ACCOUNTS_3.csv         |users_50.csv   |csv file of test accounts   |
|ACCOUNTS_4.csv         |users_100.csv  |csv file of test accounts   |
|REQUEST.log            |requests.log   |file to log requests to   	         |
|user.classpath         |src/build/jar/zjmeter.jar|Zimbra JMeter Java Library|

## Load

LOAD.TPS_PERCENT.1 to LOAD.TPS_PERCENT.5 are used in advanced chat

|Key                           |Value|Description                                 |
|------------------------------|-----|--------------------------------------------|
|LOAD.duration                 |3600 |test duration in seconds                    |
|LOAD.delay                    |0    |test start delay in seconds                 |
|LOAD.users                    |5    |concurrent users/threads to run during test |
|LOAD.rampup                   |1    |time spent in ramping up users in seconds   |
|LOAD.loopcount                |-1   |action repeated in loop (-1 means infinite) |
|LOAD.RPS.start                |1    |requests Per sec startup point              |
|LOAD.RPS.duration             |3600 |requests Per sec end point	          |
|LOAD.RPS.end                  |1    |throughput shaping timer duration in sec    |
|Load.timer.duration           |10000|timer/sleep duration in millisec            |
|LOAD.TPS_PERCENT.1            |20.0 |throughput controller in percentage         |
|LOAD.TPS_PERCENT.2            |20.0 |throughput controller in percentage         |
|LOAD.TPS_PERCENT.3            |20.0 |throughput controller in percentage         |
|LOAD.TPS_PERCENT.4            |20.0 |throughput controller in percentage         |
|LOAD.TPS_PERCENT.5            |20.0 |throughput controller in percentage         |

# Test Execution Steps


1. grab a copy of the tests

   ```
   $ get clone https://github.com/Zimbra/zm-load-testing.git 
   $ cd zm-load-testing
   ```

2. create a users.csv files of accounts that can be used for testing.

   ``` 
   $ vi users.csv
   user1,user2
   ...
   ```

3. run the test  
   note: some property files use relative paths that assume jmeter is run from the repo's top directory

   ```
   $ ant generic-mail-recall
   ```
 
4. test result files will be generated under logs folder

   ```
   $ logs/generic-mail-recall-jmeter.log   --- Jmeter Logs
   $ logs/generic-mail-recall-requests.csv --- Transaction Response Time Logs
   ```

