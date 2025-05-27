# Chat

This test provides generic Zimbra supported protocol performance testing for one to one chat and Advanced Chat

# Properties

## Environment

Protocol Support

|Test Type|Test    |Protocol|
|---------|--------|--------|
|1:1 Chat |Chat    |HTTPS   |

The environment file defines how to access the environment specific information.

|Key                    |Value          |Description                         |
|-----------------------|---------------|------------------------------------|
|CHAT.URL               |chatserver.com |Server Name   			     |
|CHAT.PASSWORD          |Password       |Password      			     |
|CHAT_ACCOUNTS.csv      |chat_users.csv |csv file of test accounts (user,password)|
|REQUEST.log            |requests.log   |file to log requests to   	     |
|user.classpath         |src/build/jar/zjmeter.jar|Zimbra JMeter Java Library|

## Load

1. LOAD.TPS_PERCENT.1 to LOAD.TPS_PERCENT.3 are used in one to one chat
2. LOAD.TPS_PERCENT.1 to LOAD.TPS_PERCENT.5 are used in advanced chat

|Key                           |Value|Description                                 |
|------------------------------|-----|--------------------------------------------|
|LOAD.duration                 |3600 |test duration in seconds                    |
|LOAD.delay                    |0    |test start delay in seconds                 |
|LOAD.CHAT.users               |20   |concurrent users/threads to run during test |
|LOAD.CHAT.rampup              |10   |time spent in ramping up users in seconds   |
|LOAD.CHAT.loopcount           |-1   |action repeated in loop (-1 means infinite) |
|LOAD.RPS.start                |200  |requests Per sec startup point              |
|LOAD.RPS.duration             |3600 |requests Per sec end point	          |
|LOAD.RPS.end                  |900  |throughput shaping timer duration in sec    |
|LOAD.TPS_PERCENT.1            |100  |throughput controller in percentage         |
|LOAD.TPS_PERCENT.2            |80   |throughput controller in percentage         |
|LOAD.TPS_PERCENT.3            |90   |throughput controller in percentage         |
|LOAD.TPS_PERCENT.4            |80   |throughput controller in percentage         |
|LOAD.TPS_PERCENT.5            |80   |throughput controller in percentage         |
|LOAD.COUNTER.START            |104050|user id counter start for advanced chat    |
|LOAD.COUNTER.INCREMENT        |1     |Increment the user id by value             |
|LOAD.COUNTER.END              |105056|user id counter end for advanced chat      |


# Test Execution Steps

One to One Chat

1. grab a copy of the tests

   ```
   $ get clone https://github.com/Zimbra/zm-load-testing.git 
   $ cd zm-load-testing
   ```

2. create a chat_users.csv file of accounts that can be used for testing.
   add chat_users.csv with test account in user,password format.

   ``` 
   $ vi chat_users.csv
   user1,userpass
   ...
   ```

3. run the test  
   note: some property files use relative paths that assume jmeter is run from the repo's top directory

   ```
   $ ant generic-1-1-chat
   ```
 
4. test result files will be generated under logs folder

   ```
   $ logs/generic-1-1-chat-jmeter.log   --- Jmeter Logs
   $ logs/generic-1-1-chat-requests.csv	--- Transaction Response Time Logs
   ```

Advanced Chat

1. grab a copy of the tests

   ```
   $ get clone https://github.com/Zimbra/zm-load-testing.git
   $ cd zm-load-testing
   ```

2. create a chat_users.csv file of accounts that can be used for testing.
   add chat_users.csv with test account in user,password format.

   ```
   $ vi chat_users.csv
   user1,userpass
   ...
   ```

3. run the test
   note: some property files use relative paths that assume jmeter is run from the repo's top directory

   ```
   $ ant generic-advanced-chat
   ```
 
4. test result files will be generated under logs folder

   ```
   $ logs/generic-1-1-chat-jmeter.log   --- Jmeter Logs
   $ logs/generic-1-1-chat-requests.csv --- Transaction Response Time Logs
   ```
