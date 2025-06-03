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

echo "$(timestamp) 🔁 Running One-to-One Chat Load Test (Baseline)..."
bash one_to_one_chat_load_test_run.sh
wait_with_progress 10

echo "$(timestamp) 📄 Converting JTL to CSV (Baseline)..."
bash jtl_to_csv_converter_one_to_one_chat_baseline.sh
wait_with_progress 10

echo "$(timestamp) 🔁 Running One-to-One Chat Load Test (Benchmark)..."
bash one_to_one_chat_load_test_run.sh
wait_with_progress 10

echo "$(timestamp) 📄 Converting JTL to CSV (Benchmark)..."
bash jtl_to_csv_converter_one_to_one_chat_benchmark.sh
wait_with_progress 10

echo "$(timestamp) 📊 Comparing response times..."
python3 compare_resp_time_one_to_one_chat.py
wait_with_progress 10

echo "$(timestamp) 🧾 Generating interactive HTML report..."
python3 csv_to_html_report_one_to_one_chat.py
wait_with_progress 10

echo "$(timestamp) 📧 Sending email with summary and report..."
python3 send_dynamic_summary_report_one_to_one_chat.py

echo "$(timestamp) ✅ 1:1 Chat Performance testing comparison pipeline completed successfully!"
