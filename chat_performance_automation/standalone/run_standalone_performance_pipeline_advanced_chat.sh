#!/bin/bash

set -e  # Exit immediately on any error

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

echo "$(timestamp) 🔁 Running Advanced Chat Load Test..."
bash advanced_chat_load_test_run.sh
wait_with_progress 10

echo "$(timestamp) 📄 Converting JTL to CSV..."
bash jtl_to_csv_converter_advanced_chat_standalone.sh
wait_with_progress 10

echo "$(timestamp) 📧 Sending email with summary and report..."
python3 send_summary_report_standalone_advanced_chat.py

echo "✅ Advanced Standalone Chat Performance testing pipeline completed successfully!"
