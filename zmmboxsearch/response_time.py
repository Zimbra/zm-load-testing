import re
import numpy as np

log_file_path = '/opt/zimbra/log/mailbox.log'

# Pattern to match lines containing "time elapsed"
pattern = r"SearchMultiMailboxRequest elapsed=(\d+)"

time_elapsed_values = []

# Read the log file and extract "time elapsed" values
with open(log_file_path, "r") as file:
    for line in file:
        match = re.search(pattern, line)
        if match and len(match.groups()) > 0:
            elapsed_time = int(match.group(1))
            time_elapsed_values.append(elapsed_time)

# Sort the time elapsed values in ascending order
time_elapsed_values.sort()
#print("Elapsed times:", time_elapsed_values)

# Calculate the 90th percentile
percentile_90 = np.percentile(time_elapsed_values, 90)

# Calculate the average
average = np.mean(time_elapsed_values)

# Print the time elapsed values and 90th percentile
print("Time Elapsed Values for SearchMultiMailboxRequest on mailbox 1:")
for time_elapsed in time_elapsed_values:
    print(time_elapsed)

print("Average:", average)
print("90th Percentile:", percentile_90)
