#!/bin/bash

test_path="/home/opc/zm-load-testing"
JMETER_DIR="/opt/apache-jmeter-5.4.3"

#Cleanup old build comparison transaction response time files
rm -rf ${test_path}/advanced_chat_build_comparison/*

commands=(
"java -jar ${JMETER_DIR}/lib/cmdrunner-2.2.jar --tool Reporter --plugin-type SynthesisReport --input-jtl /tmp/Chat/advanced_chat/generic-advanced-chat-requests.jtl --generate-csv /tmp/Chat/results/generic-advanced-chat-requests.csv"
)

# Remove unwanted transaction rows from previous results
grep -v -e 'upload_xls' -e 'upload_ppt' -e 'upload_doc' -e 'upload_image' \
  /tmp/Chat/results/generic-advanced-chat-requests.csv > \
  /tmp/Chat/results/generic-advanced-chat-requests-cleaned.csv

mv /tmp/Chat/results/generic-advanced-chat-requests-cleaned.csv \
   /tmp/Chat/results/generic-advanced-chat-requests.csv

# Run Command
for command in "${commands[@]}"; do
    $command &
done

wait

# Copy CSV file to advanced_chat_build_comparison folder for response time comparison across builds
cp /tmp/Chat/results/generic-advanced-chat-requests.csv  ${test_path}/advanced_chat_build_comparison/generic-advanced-chat-requests_standalone.csv

echo "JTL to CSV file conversion is successful..!!"
