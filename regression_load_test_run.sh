#!/bin/bash

#cleanup current test results files
rm -rf /tmp/results/*
rm -rf /tmp/result/*
rm -rf /tmp/soap/*.jtl
rm -rf /tmp/imap/*.jtl
rm -rf /tmp/lmtp/*.jtl
rm -rf /tmp/pop/*.jtl
rm -rf /tmp/eas/*.jtl
rm -rf /tmp/soap/soap_html_report/*
rm -rf /tmp/imap/imap_html_report/*
rm -rf /tmp/lmtp/lmtp_html_report/*
rm -rf /tmp/pop/pop_html_report/*
rm -rf /tmp/eas/eas_html_report/*

echo "cleanup of last test result files successful..!!"

# Function to run JMeter test
run_jmeter_test() {
    test_file=$1
    load_file=$2
    prop_file=$3
    results_file=$4
    html_report=$5
    out_files=$6

 # Check if the test is "generic-eas" and add the classpath argument if true
    if [[ "$test_file" == *"eas.jmx" ]]; then
        classpath_option="-Juser.classpath=${test_path}/src/build/jar/zjmeter.jar:${test_path}/src/build/jar/zm-sync-common.jar"
    else
        classpath_option="-Juser.classpath=${test_path}/src/build/jar/zjmeter.jar"
    fi

    nohup sh /opt/apache-jmeter-5.6.2/bin/jmeter.sh -n -Juser.classpath=/home/opc/zm-load-testing/src/build/jar/zjmeter.jar -t "$test_file" -q /home/opc/zm-load-testing/config/env.prop -q "$load_file" -q "$prop_file" -l "$results_file" -e -o "$html_report" -jmeter.save.saveservice.thread_counts=true -q /home/opc/zm-load-testing/config/extras.txt > "$out_files" &
}

# Run the JMeter tests concurrently
run_jmeter_test "/home/opc/zm-load-testing/tests/generic/zsoap/zsoap.jmx" "/home/opc/zm-load-testing/tests/generic/zsoap/load.prop" "/home/opc/zm-load-testing/tests/generic/zsoap/profiles/test.prop" "/tmp/soap/generic_zsoap.jtl" "/tmp/result/soap_html_report" "/tmp/result/generic-zsoap.out"
run_jmeter_test "/home/opc/zm-load-testing/tests/generic/imap/imap.jmx" "/home/opc/zm-load-testing/tests/generic/imap/load.prop" "/home/opc/zm-load-testing/tests/generic/imap/profiles/test.prop" "/tmp/imap/generic_imap.jtl" "/tmp/result/imap_html_report" "/tmp/result/generic-imap.out"
run_jmeter_test "/home/opc/zm-load-testing/tests/generic/lmtp/lmtp.jmx" "/home/opc/zm-load-testing/tests/generic/lmtp/load.prop" "/home/opc/zm-load-testing/tests/generic/lmtp/profiles/basic.prop" "/tmp/lmtp/generic_lmtp.jtl" "/tmp/result/lmtp_html_report" "/tmp/result/generic-lmtp.out"
run_jmeter_test "/home/opc/zm-load-testing/tests/generic/pop/pop.jmx" "/home/opc/zm-load-testing/tests/generic/pop/load.prop" "/home/opc/zm-load-testing/tests/generic/pop/profiles/basic.prop" "/tmp/pop/generic_pop.jtl" "/tmp/result/pop_html_report" "/tmp/result/generic-pop.out"
run_jmeter_test "${test_path}/tests/generic/eas/eas.jmx" "${test_path}/tests/generic/eas/load.prop" "${test_path}/tests/generic/eas/profiles/basic.prop" "/tmp/eas/generic_eas.jtl" "/tmp/result/eas_html_report" "/tmp/result/generic-eas.out"

# Wait for all tests to complete
wait

# check test logs
cat /tmp/result/*.out

# Print message when all tests have finished
echo "All tests have been completed !!"
