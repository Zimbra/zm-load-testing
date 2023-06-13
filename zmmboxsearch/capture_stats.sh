#!/bin/bash
  
# Output file
output_file="stats.log"

# Duration in seconds (1 hour = 3600 seconds)
duration=3600

# Timestamp format
timestamp_format="%Y-%m-%d %H:%M:%S"

# Start timestamp
start_timestamp=$(date +"$timestamp_format")

# End timestamp
end_timestamp=$(date -d "+$duration seconds" +"$timestamp_format")

# Function to capture system stats
capture_stats() {
    end_time=$(($(date +%s) + duration))

    while [ $(date +%s) -lt $end_time ]; do
        # Get current timestamp
        current_timestamp=$(date +"$timestamp_format")

        # Get CPU usage percentage
        cpu_usage=$(top -b -n 1 | awk '/%Cpu/ {print $2}')

        # Get memory usage percentage
        memory_usage=$(free | awk '/Mem:/ {printf "%.2f", $3/$2 * 100}')

        # Get network stats
        network_stats=$(ifstat -t -n -q 1 1 | tail -n 1)
        network_rx=$(echo "$network_stats" | awk '{print $2}')
        network_tx=$(echo "$network_stats" | awk '{print $3}')

        # Write stats to output file
        echo "$current_timestamp, CPU: $cpu_usage%, Memory: $memory_usage%, Network RX: $network_rx, Network TX: $network_tx" >> "$output_file"

        # Sleep for 1 second
        sleep 1
    done
}

# Start capturing stats
echo "Start Time: $start_timestamp"
echo "End Time: $end_timestamp"
echo "Capturing stats..."
capture_stats
echo "Stats capture complete."

