#!/bin/bash

#cleanup current test results files
rm -rf /tmp/Chat/results/*
rm -rf /tmp/Chat/result/*
rm -rf /tmp/Chat/1_to_1_chat/*.jtl
rm -rf /tmp/Chat/1_to_1_chat/1_to_1_chat_html_report/*

echo "cleanup of last test result files successful..!!"

# Function to run JMeter test
run_jmeter_test() {
    jmeter_path="/opt/apache-jmeter-5.6.2"
    test_file=$1
    load_file=$2
    results_file=$3
    html_report=$4
    out_files=$5

# Run JMeter with the appropriate classpath argument
    nohup sh ${jmeter_path}/bin/jmeter.sh -n $classpath_option -t "$test_file" -q ${test_path}/config/env.prop -q "$load_file" -l "$results_file" -e -o "$html_report" -jmeter.save.saveservice.thread_counts=true -q ${test_path}/config/extras.txt > "$out_files" &
}

# Test path for execution
test_path="/home/opc/zm-load-testing"

# Run the JMeter tests concurrently
run_jmeter_test "${test_path}/tests/generic/Chat/1_to_1_Chat_Solution.jmx" "${test_path}/tests/generic/Chat/load.prop" "/tmp/Chat/1_to_1_chat/generic-1-1-chat-requests.jtl" "/tmp/Chat/result/1_to_1_chat_html_report" "/tmp/Chat/result/generic-1-1-chat.out"

# Wait for all tests to complete
wait

# check test logs
cat /tmp/Chat/result/*.out

# Print message when all tests have finished
echo "1:1 Chat load test execution has been completed !!"
