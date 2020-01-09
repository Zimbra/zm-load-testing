# Tests
	BSP (Tests for Business, BusinessPlus, Standard and Professional editions of zimbra-X)
# Overview

	BSP Test suite defined to simulate customer environment for load testing using different user types. We have designed this test to hit the required load for BSP customer type. In this test suite we will load test against different types of BSP editions of Zimbra-X as in Professional, Standard, BusinessPlus and Business editions.

Desired load for SOAP, IMAP and POP will depend on the requirement

# Prerequisites - Zimbra-X

We need to make sure before we start creating pre-contions for BSP tests we need to enable clear text logins for IMAP and POP users. Follow the below steps to enable clear text login.

Edit zm-kubernetes/.config/global_config and add the following lines:

	zimbraImapCleartextLoginEnabled=TRUE zimbraReverseProxyImapStartTlsMode=off zimbraPop3CleartextLoginEnabled=TRUE zimbraReverseProxyPop3StartTlsMode=off zimbraImapMaxConnections=1000 zimbraPop3MaxConnections=1000 zimbraHttpNumThreads=500 zimbraHttpDosFilterMaxRequestsPerSec=300 zimbraLmtpNumThreads=100 zimbraMailPurgeSleepInterval=0

Edit ./config/mailbox_localconfig, and add the following:

	zimbra_mailbox_lock_max_waiting_threads=200

# Test setup, Pre-Conditions and Data Generation

Below are the steps that I have followed to create the users.

* Steps:

	1) You need to login to any of the mailbox pods and login with zimbra user (su - zimbra).

	Then execute the below command to create a COS for  Business Email Edition.

	zmprov cc Business zimbraFeatureMAPIConnectorEnabled FALSE zimbraFeatureMobileSyncEnabled FALSE zimbraArchiveEnabled FALSE zimbraFeatureConversationsEnabled FALSE  zimbraFeatureTaggingEnabled FALSE zimbraAttachmentsIndexingEnabled FALSE  zimbraFeatureViewInHtmlEnabled FALSE zimbraFeatureGroupCalendarEnabled FALSE zimbraFreebusyExchangeURL FALSE zimbraFeatureSharingEnabled FALSE zimbraFeatureTasksEnabled FALSE zimbraFeatureBriefcasesEnabled FALSE zimbraFeatureSMIMEEnabled FALSE zimbraFeatureVoiceEnabled FALSE zimbraFeatureManageZimlets FALSE zimbraFeatureCalendarEnabled FALSE zimbraFeatureGalEnabled TRUE

	In response you will receive a alpha numeric cos id something like this < 0a9d288d-d1e6-4f6b-a1a6-05b1e98daf57 > save this cos ID 

	Then using above recieved cos ID for Business edition we need to create users according to the requirement. These users will have access to zimbra-X Business edition features.

	Use the below command to create users (In the below command providing a range in the for loop will create those many number of users according to the numeric range provided, example: for i in (1..10) will create user accounts from users_business1@zmc.com to users_business10@zmc.com)

		for i in {1..10}; do echo "ca user_business$i@zmc.com test123" zimbraCOSid <cos id received from the above steps for Business edition>; done | zmprov -l 

	2) Then we need to create a users file for our test execution using the below command. We need execute this command from the repo -->   zm-load-testing/tests/BSP/business/zsoap

		for i in {1..10} ; do j=$(($i+1)); echo "user_business$i@zmc.com,test123,user_business$j@zmc.com"; done > users.csv  
	
	(This will create a user list file for users_business1@zmc.com to user_business10@zmc.com)

	We need to repeat the steps from 1 and 2 for creating different types of COS and set of users. For example : user_businessplus1@zmc.com, user_standard1@zmc.com and users_professional1@zmc.com.

	Follow the steps given in the link to create different cos ID on an environment: https://wiki.zimbra.com/wiki/Create_a_COS_for_Standard,_Professional,_BusinessPlus_and_Business_licenses

* Note: We need to make sure that each set of users.csv that is created for specific edition should be placed in the respective repos. 
Example : zm-load-testing/tests/BSP/businessplus/zsoap , zm-load-testing/tests/BSP/standard/zsoap  and  zm-load-testing/tests/BSP/professional/zsoap

	3) Similarly we need to create users for Imap and Pop scenarios also. The below command will create set of users for four editions of zimbra-X. In case if you plan to run the BSP test for only one single edition, then modify the below command accordingly for your requirement. Therefore execute this below command from repo zm-load-testing/config

		for i in {1..10} ; do j=$(($i+1)); echo "user_business$i@zmc.com,test123,user_business$j@zmc.com";echo "user_businessplus$i@zmc.com,test123,user_businessplus$j@zmc.com";echo "user_standard$i@zmc.com,test123,user_standard$j@zmc.com";echo "user_professional$i@zmc.com,test123,user_professional$j@zmc.com"; done >> users.csv

	The above three steps will complete user creation and providing permissions to access Zimbra-X business edition features.

	Similarly we need follow above same three steps to create users for different editions of zimbra-X (Businessplus, Standard and Professional)

	However, before executing a performance test we need to populate each user mailbox with 20 emails using the below steps 

* Email injection: Insert email using curl command using import functionality
	
		for i in {1..10} ; do curl -k -u admin:test123  --data-binary @<JmeterTestData_20Emails.tgz file location> "https://129.213.173.218:7071/service/home/<USERNAME>/?fmt=tgz&resolve=skip"

	Modify config/env.prop and config/users.csv as per test enviroment/requirement. 

	Use below command to get loadbalancers and port numbers.

		kubectl get svc (on zimbarX machine)

	We Need to make changes in load.prop for users and durations (tests/BSP/business/zsoap, tests/BSP/businessplus/zsoap, tests/BSP/standard/zsoap, tests/BSP/professional/zsoap, tests/BSP/pop  and tests/BSP/imap).

* Command to execute test:

	We can start test in two ways;

	a) Using the below ant commands we can start all BSP test individually.

      		ant BSP-business-zsoap / ant BSP-businessplus-zsoap / ant BSP-standard-zsoap / ant BSP-professonal-zsoap / ant BSP-imap / ant BSP-pop
	  
	b) Using bspruntest.sh. It will start all the test together in background.
      
		 sh bspruntest.sh   

* NOTE: In case of beefy machine we need to comment out XOIP request and change sequence number for rest requests in tests/BSP/pop/profiles/basic.prop.

	The output of all tests running in background can be monitored using this command ' tail-f *.out '

* To stop test:

		sh finishTests.sh -u BSP

	It will stop the test and take backup of required logs.
