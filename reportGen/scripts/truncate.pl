#!/usr/bin/perl
$report_dir = $ARGV[0];
$name_of_host = $ARGV[1];

open (FH, "<", "$report_dir/sorted_$name_of_host.xml") or die "Could not open $report_dir/sorted_$name_of_host.xml";
@sorted_data = <FH>;
close FH;

open (FH, ">", "$report_dir/sort.xml") or die "Could not open $report_dir/sort.xml";
print FH "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
print FH "<testResults version=\"1.2\">\n";
print FH @sorted_data;
print FH "\n</testResults>";
close FH;

open (FH, ">", "$report_dir/truncated_$name_of_host.xml") or die "Could not open $report_dir/truncated_$name_of_host.xml";
$total = 2000;
$counter = 0;
for (@sorted_data) {
  if (m/.*<httpSample.*/) {
    $counter++;
  }
  if ($counter eq $total) { 
    #print "Made it to the break\n";
    last;	
  }
  print FH $_;
}

while (m/.*<httpSample.*/) {
  print FH $_;
}
close FH;

open (FH, "<", "$report_dir/truncated_$name_of_host.xml") or die "Could not open $report_dir/truncated_$name_of_host.xml";
@current_data = <FH>;
close FH;
#Remove first and last lines of the file
for(@current_data) {s/\<testResults\>//g};
for(@current_data) {s/\<\/testResults\>//g};

open (FH, ">", "$report_dir/merged_$name_of_host.xml") or die "Could not open $report_dir/merged_$name_of_host.xml";

print FH "\n<testResults>";

print FH "\n@current_data";
print FH "\n</testResults>";

close(FH);
