import csv

test_path="/home/opc/zm-load-testing"
baseline_build = 'v10.1.7'
benchmark_build = 'v10.1.8'

# Function to read a CSV file and return the data as a list of dictionaries
def read_csv(file_name):
    with open(file_name, newline='') as csvfile:
        reader = csv.DictReader(csvfile)
        return list(reader)

# Function to calculate the degradation percentage
def calculate_degradation(build1_90, build2_90):
    if build1_90 == 0:
        return 0.0  # Avoid division by zero if the original 90% line is 0
    return ((build2_90 - build1_90) / build1_90) * 100

# Function to determine the color based on degradation percentage
def apply_color(degradation):
    if degradation > 50:
        return 'Red'       # High degradation (> 50%)
    elif 20 <= degradation <= 50:
        return 'Orange'    # Moderate degradation (20% - 50%)
    elif 0 <= degradation < 1:
        return 'Green'     # Low degradation (0% - 1%)
    elif degradation <= 0:
        return 'Dark Green'     # Negative degradation (improvement)
    else:
        return 'Negligible'      # No degradation or negligible

# Read Build 1 and Build 2 CSV files
build1_data = read_csv(f'{test_path}/build_comparison/generic_ews_baseline.csv')
build2_data = read_csv(f'{test_path}/build_comparison/generic_ews_benchmark.csv')

# Create a list to hold the final results with comparison and degradation
results = []

# Assuming both CSV files have the same 'Label' order, so we can iterate over one of them
for row1 in build1_data:
    label = row1['Label']
    # Get the corresponding row from Build 2 by matching the label
    row2 = next((row for row in build2_data if row['Label'] == label), None)

    if row2:
        # Get the '90% Line' values for both builds (ensure they're floats)
        try:
            build1_90 = float(row1['90% Line'])
            build2_90 = float(row2['90% Line'])

            # Calculate the degradation
            degradation = calculate_degradation(build1_90, build2_90)
            color = apply_color(degradation)

            # Append the result as a dictionary
            results.append({
                'Label': label,
                'Resp Time for {baseline_build}': build1_90,
                'Resp Time for {benchmark_build}': build2_90,
                'Degradation (%)': degradation,
                'Color': color
            })
        except ValueError:
            print(f"Invalid 90% Line value for {label} in one of the builds.")

# Write the results to a new CSV file
with open('/tmp/results/transaction_response_times_comparison_ews.csv', mode='w', newline='') as csvfile:
    fieldnames = ['Label', 'Resp Time for {baseline_build}', 'Resp Time for {benchmark_build}', 'Degradation (%)', 'Color']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    writer.writeheader()
    for result in results:
        writer.writerow(result)

print("Comparison results have been saved for EWS")

