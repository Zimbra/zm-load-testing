# Tests

This directory containts all the JMeter tests that have been created for testing Zimbra performance.

The tests are broken up into four groups

* CSP

# Overview

  Test suite defined to simulate customer enviroment for load testing. We have designed this test to hit the required load for CSP customer type. 
  Desired load for SOAP: 327k, IMAP: 29.3M, POP: 11.7M and SMTP: 330K

# Prerequisites - Zimbra-X

When you are deploying Zimbra-X, there are some additional global config options that should be set so that the CSP test will run properly.  Here is a quick walk-through.  It assumes that you have created a deployment branch in `zm-kubernetes` that is based on the latest `develop` branch.  The walk-through does _not_ cover all possible options and settings that you may need.  It documents just the configuration needed for the CSP tests.

Before deploying (select the value for TEMPLATE_ENV that you desire)

    export TEMPLATE_ENV=templates/base.env
    make TEMPLATE_ENV=${TEMPLATE_ENV} clean init-keys init-configs init-passwords

Edit `.config/global_config` and add the following lines:

    zimbraImapCleartextLoginEnabled=TRUE
    zimbraReverseProxyImapStartTlsMode=off
    zimbraPop3CleartextLoginEnabled=TRUE
    zimbraReverseProxyPop3StartTlsMode=off
    zimbraImapMaxConnections=1000
    zimbraPop3MaxConnections=1000
    zimbraHttpNumThreads=500
    zimbraHttpDosFilterMaxRequestsPerSec=300
    zimbraLmtpNumThreads=100
    zimbraMailPurgeSleepInterval=0

Edit `./config/mailbox_localconfig`, and add the following:

        zimbra_mailbox_lock_max_waiting_threads=200

# Test setup 

* Data Generation
  
  User creation: Login to any of the mailbox pod, with zimbra user start account creation using below commands:
  for i in {1..20000}; do echo "ca user$i@zmc.com test123" >>/tmp/ac; done
  zmprov -l </tmp/ac

  Email injection: Insert email using curl command using import functionality.
  curl -k -u admin:test123  --data-binary @<JmeterTestData_20Emails.tgz file location> "https://129.213.173.218:7071/service/home/<USERNAME>/?fmt=tgz&resolve=skip"

*  Modify config/env.prop and config/users.csv as per test enviroment/requirement. Use below command to get loadbalancers and port numbers.
   kubectl get svc (on zimbarX machine)

*  Need to make changes in load.prop for users and durations (tests/CSP/imap, tests/CSP/pop and tests/CSP/zsoap).

* Command to execute test:
  We can start test in two ways;
    a) Using ant command. In this we will need to the start all three test individually.
            ant CSP-imap / ant CSP-zsoap / ant CSP-pop
    b) Using runtest.sh. It will start all the test together in background.
            sh runtest.sh -u CSP 

* NOTE: In case of beefy machine we need to comment out XOIP request and change sequence number for rest requests in tests/CSP/pop/profiles/basic.prop.

* To stop test:
  sh finishTests.sh -u CSP
  It will stop the test and take backup of required logs.

* BSP

# Overview

  Test suite defined to simulate customer enviroment for load testing. We have designed this test to hit the required load for BSP customer type.
  Desired load for SOAP: 327k, IMAP: 29.3M, POP: 11.7M and SMTP: 330K

# Prerequisites - Zimbra-X

When you are deploying Zimbra-X, there are some additional global config options that should be set so that the BSP test will run properly.  Here is a quick walk-through.  It assumes that you have created a deployment branch in `zm-kubernetes` that is based on the latest `develop` branch.  The walk-through does _not_ cover all possible options and settings that you may need.  It documents just the configuration needed for the BSP tests.

Before deploying (select the value for TEMPLATE_ENV that you desire)

    export TEMPLATE_ENV=templates/base.env
    make TEMPLATE_ENV=${TEMPLATE_ENV} clean init-keys init-configs init-passwords

Edit `.config/global_config` and add the following lines:

    zimbraImapCleartextLoginEnabled=TRUE
    zimbraReverseProxyImapStartTlsMode=off
    zimbraPop3CleartextLoginEnabled=TRUE
    zimbraReverseProxyPop3StartTlsMode=off
    zimbraImapMaxConnections=1000
    zimbraPop3MaxConnections=1000
    zimbraHttpNumThreads=500
    zimbraHttpDosFilterMaxRequestsPerSec=300
    zimbraLmtpNumThreads=100
    zimbraMailPurgeSleepInterval=0

Edit `./config/mailbox_localconfig`, and add the following:

        zimbra_mailbox_lock_max_waiting_threads=200

# Test setup

* Data Generation

  User creation: Login to any of the mailbox pod, with zimbra user start account creation using below commands:
  for i in {1..20000}; do echo "ca user$i@zmc.com test123" >>/tmp/ac; done
  zmprov -l </tmp/ac

  Email injection: Insert email using curl command using import functionality.
  curl -k -u admin:test123  --data-binary @<JmeterTestData_20Emails.tgz file location> "https://129.213.173.218:7071/service/home/<USERNAME>/?fmt=tgz&resolve=skip"

*  Modify config/env.prop and config/users.csv as per test enviroment/requirement. Use below command to get loadbalancers and port numbers.
   kubectl get svc (on zimbarX machine)

*  Need to make changes in load.prop for users and durations (tests/BSP/imap, tests/BSP/pop and tests/BSP/zsoap).

* Command to execute test:
  We can start test in two ways;
    a) Using ant command. In this we will need to the start all three test individually.
            ant BSP-imap / ant BSP-zsoap / ant BSP-pop
    b) Using runtest.sh. It will start all the test together in background.
            sh runtest.sh -u BSP

* NOTE: In case of beefy machine we need to comment out XOIP request and change sequence number for rest requests in tests/BSP/pop/profiles/basic.prop.

* To stop test:
  sh finishTests.sh -u BSP
  It will stop the test and take backup of required logs.

* fixed
  
  The fixed tests can be run against different environments and with different loads but what the test does is fixed inside the jmx file.
  
  A typical execution of a fixed test is of the form:
  
  ```
  jmeter -q env.prop -q load.prop -t test.jmx
  ```
  
  For more details and example of running fixed tests see fixed [README.md](fixed/README.md).

* generic 
  
  The generic tests can be run against different environments, with different loads, and using a profile can perform different sequences of protocol commands.
  
  All the generic tests depend on the [Zimbra JMeter Java Library](../src/README.md) to generate the sequence of commands from the profile property file.
  
  A typical execution of a generic test is of the form:
  
  ```
  jmeter -q env.prop -q load.prop -q profile.prop -t test.jmx
  ```
  
  For more details and example of running generic tests see generic [README.md](generic/README.md).

# Scaling

Generally most of theses tests will execute up to about 500 users. However to go beyond that may require adjusting the amount of memory allocated to jmeter. Adjusting the memory size can usually get you up to about 1000 users. However going beyond may result in dramatically increased run times.  Running multiple instanses of jmeter to go beyond 1000 users was found to maintain reasonable runtimes.

With the above in mind although "mix" tests are available those tests are only usefull for very low user numbers and often times it is simpler to just run multiple instances of jmeter each running different protocols. Suppose you want 2000 imap users, 1000 smtp users, and 5000 zsoap users this would generally be done with:

* 2 instances of jmeter imap 1000 users

* 1 instance of jmeter smtp 1000 users

* 5 instances of jmeter zsoap 1000 users

Start all 8 of the above instances at the same time against the same environment.
