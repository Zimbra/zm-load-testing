#!/bin/bash

set -e  # Exit on any error

# Utility: Wait with progress
wait_with_progress() {
    local seconds=$1
    echo -n "⏳ Waiting $seconds seconds: "
    for ((i=1; i<=seconds; i++)); do
        echo -n "."
        sleep 1
    done
    echo " ✅"
}

timestamp() {
  date +"%Y-%m-%d %H:%M:%S"
}

echo "$(timestamp) 🔁 Running One-to-One Chat Load Test..."
bash one_to_one_chat_load_test_run.sh
wait_with_progress 10

echo "$(timestamp) 📄 Converting JTL to CSV..."
bash jtl_to_csv_converter_one_to_one_chat_standalone.sh
wait_with_progress 10

echo "$(timestamp) 📧 Sending email with summary and report..."
python3 send_summary_report_standalone_one_to_one_chat.py

echo "$(timestamp) ✅ 1:1 Chat Stanadalone Performance testing pipeline completed successfully!"
