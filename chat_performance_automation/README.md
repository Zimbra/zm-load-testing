# Chat

This test provides zimbra supported automated chat performance testing for one to one chat and Advanced Chat.

# Features

1. Automated performance chat execution for standalone and release comparison tests for one to one and advanced chat
2. Performance scripts integrated with InfluxDB & Grafana providing live test monitoring support
3. Test reports are generated on the go and sent to user mailbox.
4. Release Comparison test reports showing transaction response time comparison for baseline vs benchmark builds.
   The transactions are highlighted as Dark Green, Green, Orange and Red where :
      * Dark Green means Improved response time (Negative degradation (improvement))
      * Green means Good response time (Low degradation 0% - 1%)
      * Orange means Moderate Degradation in response time (degradation 1% - 10%)
      * Red means Regressed Transaction (High degradation > 10%)

## Flow Diagram

               +------------------+
               |  JMeter Client   |
               |------------------|
               |     JMeter       |
               |    InfluxDB      |
               |     Grafana      |
               +--------+---------+
                        |
                        v
               +--------+--------+
               |    ZCS Server   |
               +--------+--------+
                        |
                        v
               +--------+--------+
               |   Chat Server   |
               +-----------------+

# Properties

## Environment

Protocol Support

|Test Type |Test          |Protocol|
|----------|--------------|--------|
|Chat      |1:1 Chat      |HTTPS   |
|Chat      |advanced Chat |HTTPS   |

The environment file defines how to access the environment specific information.

|Key                    |Value           |Description                         |
|-----------------------|----------------|------------------------------------|
|CHAT.URL               |chatserver.com  |Server Name   		                  |
|CHAT.PASSWORD          |Password        |Password      		                  |
|CHAT_ACCOUNTS.csv      |chat_users.csv  |csv file of test accounts (user,password)|
|INFLUXDB.server        |influxdb.server |Influxdb Server Name                |
|INFLUXDB.port          |8086            |Influxdb Port                       |
|REQUEST.log            |requests.log    |file to log requests to   	        |
|user.classpath         |src/build/jar/zjmeter.jar|Zimbra JMeter Java Library |

The environment file in chat performance automation defines email configs and other test details to be sent to user.

|Key                           |Value        |Description                       |
|------------------------------|-------------|----------------------------------|
|EMAIL_HOST                    |smtp.server  |Hostname                    	    |
|EMAIL_PORT                    |587    	     |Port                 		          |
|EMAIL_USER                    |user@test.com|user email 			                  |
|EMAIL_PASS                    |password     |Password   			                  |
|EMAIL_TO	                     |perf@test.com|user email recipient 		          |
|CONCURRENT_USERS              |100          |Concurrent users              	  |
|TEST_DURATION_MINUTES         |60 	         |test duration in minutes          |
|test_path                     |<test_path>  |Repo base path    		            |

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

Standalone Chat Performance Test Automation:

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

3. run the test - One to One Chat
   note: some property files use relative paths that assume jmeter is run from the repo's top directory

   ```
   $ nohup run_standalone_performance_pipeline_one_to_one_chat.sh > /tmp/standalone_one_to_one_chat.log
   ```

4. run the test - Advanced Chat
   note: some property files use relative paths that assume jmeter is run from the repo's top directory

   ```
   $ nohup run_standalone_performance_pipeline_advanced_chat.sh > /tmp/standalone_advanced_chat.log
   ```


Comparison(baseline_vs_benchmark) Chat Performance Test Automation:

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

3. run the test - One to One Chat
   note: some property files use relative paths that assume jmeter is run from the repo's top directory

   ```
   $ nohup run_full_performance_pipeline_one_to_one_chat.sh > /tmp/comparison_one_to_one_chat.log &
   ```
 
4. run the test - Advanced Chat
   note: some property files use relative paths that assume jmeter is run from the repo's top directory

   ```
   $ nohup run_full_performance_pipeline_advanced_chat.sh > /tmp/comparison_advanced_chat.log &
   ```

