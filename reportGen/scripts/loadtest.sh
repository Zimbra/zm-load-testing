#!/bin/sh
#date=`date +%m\-%d\-%Y`
#connection_timeout=""
#response_timeout=""
usage=$(
cat <<EOF
$0 [OPTION]
-w VALUE    set "w" argument to workspace
-t VALUE    set "t" argument for test to execute
EOF
)
#define default values
test_name=""
hostname=""
workspace=""

while getopts "w:t:cv" OPTION; do
  case "$OPTION" in
    w)
      workspace="$OPTARG"
      ;;
    t)
      test_name="$OPTARG"
      ;;
    *)
      echo "unrecognized option"
      echo "$usage"
      ;;
  esac
done

env_file=$workspace'/config/env.prop'

while IFS='=' read -r value1 value2 ; do
var2=$(echo $value1 | cut -f2 -d.)
    if [ $var2 = "server" ]; then
        hostname=$value2
        break
    fi
done < "$env_file"

userFound=''
while IFS='=' read -r value1 value2 ; do
    if [ $value1 = "ACCOUNTS.csv" ]; then
        userFound='yes'
    else 
        userFound='no'
    fi
done < "$env_file"

if [ $userFound = 'no' ]; then
    echo "ACCOUNTS.csv=$workspace/config/users.csv" >> $env_file
fi

folder_name=$(echo $test_name | cut -f1 -d-)
jmx_file=$(echo $test_name | cut -f2 -d-)
jmeter=/opt/apache-jmeter-3.3/bin/jmeter
if [ ! -x "${jmeter}" ]
then
    jmeter=`which jmeter`
fi

`ssh root@$hostname date --set="$(ssh root@$hostname su - zimbra -c date)"`
(set -x;`date --set="$(ssh root@$hostname date)"`)
version=$(ssh root@$hostname "su - zimbra -c 'zmcontrol -v |cut -c9-21|sed -r s/[.]+//g'")
report_dir=${workspace}/result/${test_name}_${version}_$(date +'%m-%d_%H-%M')
echo "test name->" $test_name
echo "workspace->" $workspace
echo "hostname->" $hostname
echo "report_dir->" $report_dir
echo "**************Test Started**************"
mkdir -p $report_dir
date1=`ssh root@$hostname date +%x`
date2=`ssh root@$hostname date +%T`
startDate="$date1 $date2"

#startDate=`date +'%x %H:%M:%S'`
echo "Performance test started at :- $startDate"
if [ $folder_name = "generic"]; then {
echo "${jmeter} -n -Juser.classpath=${workspace}/src/build/jar/zjmeter.jar -t ${workspace}/tests/${folder_name}/${jmx_file}/${jmx_file}.jmx -q ${workspace}/config/env.prop -q ${workspace}/tests/${folder_name}/${jmx_file}/load.prop -q ${workspace}/tests/${folder_name}/${jmx_file}/profiles/basic.prop -l ${report_dir}/${test_name}.xml -Djmeter.save.saveservice.thread_counts=true -Djmeter.save.saveservice.output_format=xml"
${jmeter} -n -Juser.classpath=${workspace}/src/build/jar/zjmeter.jar -t ${workspace}/tests/${folder_name}/${jmx_file}/${jmx_file}.jmx -q ${workspace}/config/env.prop -q ${workspace}/tests/${folder_name}/${jmx_file}/load.prop -q ${workspace}/tests/${folder_name}/${jmx_file}/profiles/basic.prop -l ${report_dir}/${test_name}.xml -Djmeter.save.saveservice.thread_counts=true -Djmeter.save.saveservice.output_format=xml
}
else {
echo "${jmeter} -n -Juser.classpath=${workspace}/src/build/jar/zjmeter.jar -t ${workspace}/tests/${folder_name}/${jmx_file}/${jmx_file}.jmx -q ${workspace}/config/env.prop -q ${workspace}/tests/${folder_name}/${jmx_file}/load.prop -l ${report_dir}/${test_name}.xml -Djmeter.save.saveservice.thread_counts=true -Djmeter.save.saveservice.output_format=xml"
${jmeter} -n -Juser.classpath=${workspace}/src/build/jar/zjmeter.jar -t ${workspace}/tests/${folder_name}/${jmx_file}/${jmx_file}.jmx -q ${workspace}/config/env.prop -q ${workspace}/tests/${folder_name}/${jmx_file}/load.prop -l ${report_dir}/${test_name}.xml -Djmeter.save.saveservice.thread_counts=true -Djmeter.save.saveservice.output_format=xml
}
 fi
wait
date3=`ssh root@$hostname date +%x`
date4=`ssh root@$hostname date +%T`
endDate="$date3 $date4"

echo "Performance test ended at :- $endDate"
echo "****Taking Test Data Backup****"

ssh root@$hostname "/opt/zimbra/libexec/zmdiaglog -a"
zmdiaglog_dir=$(ssh root@$hostname "ls -lat /opt/zimbra/data/tmp/" | grep zmdia*| head -1|awk '{print $9}')
ssh root@$hostname "su - zimbra -c 'mkdir -m 777 /opt/zimbra/data/tmp/$zmdiaglog_dir/zmstat_charts'"
echo "su - zimbra -c '/opt/zimbra/bin/zmstat-chart --start-at \"$startDate\" --end-at \"$endDate\" -d /opt/zimbra/data/tmp/$zmdiaglog_dir/zmstat_charts -s /opt/zimbra/data/tmp/$zmdiaglog_dir/stats'"
ssh root@$hostname "su - zimbra -c '/opt/zimbra/bin/zmstat-chart --start-at \"$startDate\" --end-at \"$endDate\" -d /opt/zimbra/data/tmp/$zmdiaglog_dir/zmstat_charts -s /opt/zimbra/data/tmp/$zmdiaglog_dir/stats/'"
ssh root@$hostname "chmod -R 777 /opt/zimbra/data/tmp/$zmdiaglog_dir"
images=$report_dir/images
echo "***********Creating Jmeter report images***********"
mkdir -m 777 $report_dir/images
${jmeter} -n -t $workspace/reportGen/scripts/GraphsGenerator.jmx -Jxml_file=${report_dir}/${test_name}.xml -Jimages=$images &
wait
echo "***********Creating Jmeter report File***********"
report_xsl=$workspace/reportGen/xsl_files/jmeter-results-detail-report_21.xsl
simplereport_xsl=$workspace/reportGen/xsl_files/jmeter-results-percentile-report_21.xsl
xsl_dir=$workspace/reportGen/xsl_files
#sort initial
echo "$workspace/reportGen/scripts/sort_results.pl $report_dir ${test_name}"
$workspace/reportGen/scripts/sort_results.pl $report_dir ${test_name}

echo "/usr/bin/java -Xmx4182m -jar $workspace/reportGen/scripts/jars/saxon9he.jar -o:$report_dir/Transaction_detail.html -xsl:$report_xsl -s:$report_dir/sample_sorted_${test_name}.xml"
/usr/bin/java -Xmx4182m -jar $workspace/reportGen/scripts/jars/saxon9he.jar -o:$report_dir/transaction.html -xsl:$report_xsl -s:$report_dir/sample_sorted_${test_name}.xml

#truncate now merges as well for history purposes.
echo "/usr/bin/perl $workspace/reportGen/scripts/truncate.pl ${report_dir} ${test_name}"
/usr/bin/perl $workspace/reportGen/scripts/truncate.pl  ${report_dir} ${test_name}

#sort again after truncate and merge
echo "/usr/bin/java -Xmx4182m -jar $workspace/reportGen/scripts/jars/saxon9he.jar  -o:$report_dir/truncated_and_sorted_${test_name}.xml -xsl:$xsl_dir/sort_output.xsl -s:$report_dir/merged_${test_name}.xml"
/usr/bin/java -Xmx4182m -jar $workspace/reportGen/scripts/jars/saxon9he.jar  -o:$report_dir/truncated_and_sorted_${test_name}.xml -xsl:$xsl_dir/sort_output.xsl -s:$report_dir/merged_${test_name}.xml

echo "/usr/bin/java -Xmx4182m -jar $workspace/reportGen/scripts/jars/saxon9he.jar -o:$report_dir/sample.html -xsl:$simplereport_xsl -s:$report_dir/sort.xml
rm -rf $report_dir/truncated_* $report_dir/merged* $report_dir/sample_sorted*"
/usr/bin/java -Xmx4182m -jar $workspace/reportGen/scripts/jars/saxon9he.jar -o:$report_dir/sample.html -xsl:$simplereport_xsl -s:$report_dir/sort.xml

rm -rf $report_dir/truncated_* $report_dir/merged* $report_dir/sample_sorted*
scp -r root@$hostname:/opt/zimbra/data/tmp/$zmdiaglog_dir $report_dir
echo "****Test Completed Successfully****"
echo "****Test Result Analysis****"
touch $report_dir/AnalysisReport
echo "" >> $report_dir/AnalysisReport
echo "****Test Parameters****" >> $report_dir/AnalysisReport
echo "Test :"${test_name} >> $report_dir/AnalysisReport
echo "Start Time:"$startDate >> $report_dir/AnalysisReport
echo "End Time:"$endDate >> $report_dir/AnalysisReport
echo "Hostname:"$hostname >> $report_dir/AnalysisReport
echo "" >> $report_dir/AnalysisReport
echo "***Jmeter Result Analysis***" >> $report_dir/AnalysisReport
echo "**Soap Results**" >> $report_dir/AnalysisReport
rm -rf /tmp/stat /tmp/stat_val
sed -n -e '51,58p' $report_dir/sample.html | sed -n '/^$/!{s/<[^>]*>//g;p;}' > /tmp/stat
sed -n -e '61,68p' $report_dir/sample.html | sed -n '/^$/!{s/<[^>]*>//g;p;}' > /tmp/stat_val
paste /tmp/stat /tmp/stat_val | column -s $'/\t' -t | sed 's/output//g'| sed 's/_val//g' >>  $report_dir/AnalysisReport
success=`sed -n -e '63p' $report_dir/sample.html | sed -n '/^$/!{s/<[^>]*>//g;p;}'| sed -e 's/ //g'|sed 's/\%//g'`
if [ $success > 95.00 ]; then
    flag=Pass
else
    flag=Fail
    echo "Test failed as Error % increases 5%"
fi
echo "Test Status (from soap success rate):"$flag >> $report_dir/AnalysisReport

echo "" >> $report_dir/AnalysisReport
echo "***zmstat-chart Analysis***" >> $report_dir/AnalysisReport

cat $report_dir/$zmdiaglog_dir/zmstat_charts/summary.txt >> $report_dir/AnalysisReport
echo "===========================================" >> $report_dir/AnalysisReport
$workspace/reportGen/scripts/transpose_summary.pl $report_dir/$zmdiaglog_dir/ | sed 's/,/ /g' | grep cpu.csv:cpu: >>  $report_dir/AnalysisReport
echo "===========================================" >> $report_dir/AnalysisReport
$workspace/reportGen/scripts/transpose_summary.pl $report_dir/$zmdiaglog_dir/ | sed 's/,/ /g' | grep soap.csv |grep avg  | sed 's/soap\.csv:group-plot-synthetic\$//g' >>  $report_dir/AnalysisReport
echo "===========================================" >> $report_dir/AnalysisReport
$workspace/reportGen/scripts/transpose_summary.pl $report_dir/$zmdiaglog_dir/ | sed 's/,/ /g' | grep imap >>  $report_dir/AnalysisReport
echo "===========================================" >> $report_dir/AnalysisReport
$workspace/reportGen/scripts/transpose_summary.pl $report_dir/$zmdiaglog_dir/ | sed 's/,/ /g' | grep lmtp >>  $report_dir/AnalysisReport
echo "===========================================" >> $report_dir/AnalysisReport
date2=`echo "$date2"|sed '$s/..$//'`
`sed -e "1,/$date2/ d" $report_dir/$zmdiaglog_dir/logs/mailbox.log > $report_dir/$zmdiaglog_dir/logs/mailbox1.log`
mv $report_dir/$zmdiaglog_dir/zmstat_charts/ $report_dir/zmstat_charts/
echo "/usr/bin/java -Xmx3200m -cp $workspace/reportGen/scripts/jars/Loganalyzer.jar com.zimbra.perf.loganalyzer.ExceptionsAnalyzer $report_dir/Test $report_dir/$zmdiaglog_dir/logs/mailbox1.log"
/usr/bin/java -Xmx3200m -cp $workspace/reportGen/scripts/jars/Loganalyzer.jar com.zimbra.perf.loganalyzer.ExceptionsAnalyzer $report_dir/Test $report_dir/$zmdiaglog_dir/logs/mailbox1.log
echo "/usr/bin/java -Xmx3200m -cp $workspace/reportGen/scripts/jars/Loganalyzer.jar com.zimbra.perf.loganalyzer.LogAnalyzer $report_dir/$zmdiaglog_dir/logs/mailbox1.log > $report_dir/TestLogs.txt"
/usr/bin/java -Xmx3200m -cp $workspace/reportGen/scripts/jars/Loganalyzer.jar com.zimbra.perf.loganalyzer.LogAnalyzer $report_dir/$zmdiaglog_dir/logs/mailbox1.log > $report_dir/TestLogs.txt
echo "/usr/bin/java -Xmx3200m -cp $workspace/reportGen/scripts/jars/Loganalyzer.jar com.zimbra.perf.loganalyzer.ExceptionsAnalyzer $report_dir/Test $report_dir/$zmdiaglog_dir/logs/mailbox1.log"
echo "/usr/bin/java -Xmx3200m -cp $workspace/reportGen/scripts/jars/Loganalyser.jar com.zimbra.perf.loganalyser.SlowSQLAnalyzer $report_dir/TestSlow $report_dir/$zmdiaglog_dir/logs/mailbox1.log"
/usr/bin/java -Xmx3200m -cp $workspace/reportGen/scripts/jars/Loganalyzer.jar com.zimbra.perf.loganalyzer.SlowSQLAnalyzer $report_dir/TestSlow $report_dir/$zmdiaglog_dir/logs/mailbox1.log
CPU_Usage=`sed -n -e '60p' $report_dir/AnalysisReport | grep -o '......$'`
CPU_Usage=$CPU_Usage s/\D//g
BaseCPU=85
if [ $CPU_Usage -lt $BaseCPU ]; then
    flag_cpu=Pass
else
    flag_cpu=Fail
fi
BaseDISK=85.00
flag_disk=Pass
grep Util $report_dir/zmstat_charts/summary.txt | awk -F: '{print $2}' | while read -r line ; do
if [ $line \> $BaseDISK ]; then
    flag_disk=Fail
    break  
else
    flag_disk=Pass
fi
done
rm -rf $report_dir/$zmdiaglog_dir
cat $report_dir/AnalysisReport
echo "Test Completed with Disk Utilization status: "$flag_disk
echo "Test Completed with Error % status:"$flag
echo "Test Completed with CPU utilization status:"$flag_cpu
