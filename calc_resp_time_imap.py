import csv

# Function to read the response time and transaction name from the CSV file
def get_response_time(csv_file):
    response_time = {}
    with open(csv_file, 'r') as file:
        reader = csv.DictReader(file)
        for row in reader:
            # Adjust the column names below if needed
            transaction_name = row['Label']
            elapsed_time = float(row['90% Line'])
            response_time[transaction_name] = elapsed_time
    return response_time

# Compare response times for two builds
def compare_builds(build1_csv, build2_csv):
    build1_response_time = get_response_time(build1_csv)
    build2_response_time = get_response_time(build2_csv)
    
    # Calculate the average response time for each build
    build1_avg_response_time = sum(build1_response_time.values()) / len(build1_response_time)
    build2_avg_response_time = sum(build2_response_time.values()) / len(build2_response_time)
    
    # Compare the average response times
    if build1_avg_response_time < build2_avg_response_time:
        print("Build 1 has a lower average response time.")
    elif build1_avg_response_time > build2_avg_response_time:
        print("Build 2 has a lower average response time.")
    else:
        print("Both builds have the same average response time.")
    
    # Calculate percent change for each transaction
    transaction_percent_change = {}
    for transaction_name in build1_response_time:
        if transaction_name in build2_response_time:
            response_time_build1 = build1_response_time[transaction_name]
            response_time_build2 = build2_response_time[transaction_name]
            percent_change = ((response_time_build2 - response_time_build1) / response_time_build1) * 100
            transaction_percent_change[transaction_name] = percent_change
    
    # Publish the transaction response times with percent change in CSV format
    output_csv = '/tmp/results/transaction_response_times_comparison_imap.csv'
    with open(output_csv, 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(['Transaction', 'Build 1 Response Time (ms)', 'Build 2 Response Time (ms)', 'Percent Change'])
        for transaction_name in build1_response_time:
            if transaction_name in build2_response_time:
                response_time_build1 = build1_response_time[transaction_name]
                response_time_build2 = build2_response_time[transaction_name]
                percent_change = transaction_percent_change.get(transaction_name, 0)
                
                # Set the color based on percent change
                #if percent_change > 10:
                 #   percent_change_color = '\033[91m'  # Red
                #elif percent_change > 5:
                 #   percent_change_color = '\033[93m'  # Orange
                #else:
                 #   percent_change_color = '\033[92m'  # Green
               
		 # Remove the ANSI escape codes and store the percentage change without color
                percent_change_without_color = str(percent_change) + '%'

                writer.writerow([transaction_name, response_time_build1, response_time_build2, percent_change_without_color])    
    print(f"Transaction response times with percent change have been published in {output_csv}.")

# Specify the CSV files for the two builds
build1_csv_file = '/home/opc/zm-load-testing/build_comparison/generic_imap_B1.csv'
build2_csv_file = '/home/opc/zm-load-testing/build_comparison/generic_imap_B2.csv'

# Compare the response times for the two builds and publish in CSV format
compare_builds(build1_csv_file, build2_csv_file)

