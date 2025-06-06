#!/bin/bash

test_path="/home/opc/zm-load-testing"
JMETER_DIR="/opt/apache-jmeter-5.4.3"

commands=(
"java -jar ${JMETER_DIR}/lib/cmdrunner-2.2.jar --tool Reporter --plugin-type SynthesisReport --input-jtl /tmp/soap/generic_zsoap.jtl --generate-csv /tmp/results/generic_zsoap.csv"
"java -jar ${JMETER_DIR}/lib/cmdrunner-2.2.jar --tool Reporter --plugin-type SynthesisReport --input-jtl /tmp/imap/generic_imap.jtl --generate-csv /tmp/results/generic_imap.csv"
"java -jar ${JMETER_DIR}/lib/cmdrunner-2.2.jar --tool Reporter --plugin-type SynthesisReport --input-jtl /tmp/pop/generic_pop.jtl --generate-csv /tmp/results/generic_pop.csv"
"java -jar ${JMETER_DIR}/lib/cmdrunner-2.2.jar --tool Reporter --plugin-type SynthesisReport --input-jtl /tmp/lmtp/generic_lmtp.jtl --generate-csv /tmp/results/generic_lmtp.csv"
"java -jar ${JMETER_DIR}/lib/cmdrunner-2.2.jar --tool Reporter --plugin-type SynthesisReport --input-jtl /tmp/eas/generic_eas.jtl --generate-csv /tmp/results/generic_eas.csv"
"java -jar ${JMETER_DIR}/lib/cmdrunner-2.2.jar --tool Reporter --plugin-type SynthesisReport --input-jtl /tmp/ews/generic_ews.jtl --generate-csv /tmp/results/generic_ews.csv"
)

# Run commands in parallel
for command in "${commands[@]}"; do
    $command &
done

# Wait for all commands to finish
wait

# Copy CSV file to build_comparison folder for response time comparison across build
cp /tmp/results/generic_zsoap.csv ${test_path}/build_comparison/generic_zsoap_benchmark.csv
cp /tmp/results/generic_imap.csv ${test_path}/build_comparison/generic_imap_benchmark.csv
cp /tmp/results/generic_pop.csv ${test_path}/build_comparison/generic_pop_benchmark.csv
cp /tmp/results/generic_lmtp.csv ${test_path}/build_comparison/generic_lmtp_benchmark.csv
cp /tmp/results/generic_eas.csv ${test_path}/build_comparison/generic_eas_benchmark.csv
cp /tmp/results/generic_ews.csv ${test_path}/build_comparison/generic_ews_benchmark.csv

echo "JTL to CSV conversion is successful..!!"
