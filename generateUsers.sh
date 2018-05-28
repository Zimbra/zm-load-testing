#!/bin/sh
userCount=`grep USER.COUNT config/env.prop|cut -d'=' -f2`
emailCount=`grep EMAIL.COUNT config/env.prop|cut -d'=' -f2`
echo "$userCount users will be created with $emailCount emails per mailbox"
random=`shuf -i 0-1000000000 -n 1`
count=1
nxtCount=2
timestamp=`date +%s`
>config/users.csv
while [ $count -lt $userCount ]
do
	echo "user$count""_$timestamp,test$random,user$nxtCount""_$timestamp" >>config/users.csv
	count=`expr $count + 1`
	nxtCount=`expr $nxtCount + 1`
done
	echo "user$count""_$timestamp,test$random,user1""_$timestamp" >>config/users.csv
