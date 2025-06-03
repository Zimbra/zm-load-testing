#!/bin/bash

test_path="/home/opc/zm-load-testing"
JMETER_DIR="/opt/apache-jmeter-5.4.3"

commands=(
"java -jar ${JMETER_DIR}/lib/cmdrunner-2.2.jar --tool Reporter --plugin-type SynthesisReport --input-jtl /tmp/Chat/1_to_1_chat/generic-1-1-chat-requests.jtl --generate-csv /tmp/Chat/results/generic-1-1-chat-requests.csv"
)

# Run commands in parallel
for command in "${commands[@]}"; do
    $command &
done

# Wait for all commands to finish
wait

# Copy CSV file to one_to_one_chat_build_comparison folder for response time comparison across build
cp /tmp/Chat/results/generic-1-1-chat-requests.csv  ${test_path}/one_to_one_chat_build_comparison/generic-1-1-chat-requests_benchmark.csv

echo "JTL to CSV file conversion is successful..!!"
