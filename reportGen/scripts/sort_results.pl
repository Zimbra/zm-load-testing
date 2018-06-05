#!/usr/bin/perl

$report_dir=$ARGV[0];
$jmx_log_file=$ARGV[1];

open (fh,"<","$report_dir/$jmx_log_file.xml") or die "Could not open $report_dir/$jmx_log_file.xml";
@results = <fh>;
close fh;

@sample_results = ();
@http_sample_results = ();

for (@results) {
#  if (/\<sample|\<\/sample/) {
  if (/\<sample/) {
     push(@sample_results, $_);
  }
  elsif (/\<httpSample/) {
     push(@http_sample_results,$_);
  }
}

@sample_list = ();
$row_counter = 0;
for (@sample_results) {
  if (/sample/) {
    ($t)  = $_ =~ /t\=\"(\d+)\"/;
    ($lt) = $_ =~ /lt\=\"(\d+)\"/;
    ($ts) = $_ =~ /ts\=\"(\d+)\"/;
    ($s)  = $_ =~ /\ss\=\"(\w+)\"/;
    ($lb) = $_ =~ /lb\=\"(\S+)\"/;
    ($rc) = $_ =~ /rc\=\"(\d+)\"/;
    ($rm) = $_ =~ /rm\=\"(\w+)\"/;
    ($tn) = $_ =~ /tn\=\"([\w|\s|\-]+).*\"/;
    ($dt) = $_ =~ /dt\=\"(\w+)\"/;
    ($by) = $_ =~ /by\=\"(\d+)\"/;
    ($ng) = $_ =~ /ng\=\"(\d+)\"/;
    ($na) = $_ =~ /na\=\"(\d+)\"/;

    push(@{ $sample_list[$row_counter] },$t,$lt,$ts,$s,$lb,$rc,$rm,$tn,$dt,$by,$ng,$na);
    $row_counter++;

  }
}

@http_sample_list = ();
$row_counter = 0;
for (@http_sample_results) {
  if (/httpSample/) {
    ($t)  = $_ =~ /t\=\"(\d+)\"/;
    ($lt) = $_ =~ /lt\=\"(\d+)\"/;
    ($ts) = $_ =~ /ts\=\"(\d+)\"/;
    ($s)  = $_ =~ /\ss\=\"(\w+)\"/;
    ($lb) = $_ =~ /lb\=\"(\S+)\"/;
    ($rc) = $_ =~ /rc\=\"(\d+)\"/;
    ($rm) = $_ =~ /rm\=\"(\w+)\"/;
    ($tn) = $_ =~ /tn\=\"([\w|\s|\-]+).*\"/;
    ($dt) = $_ =~ /dt\=\"(\w+)\"/;
    ($by) = $_ =~ /by\=\"(\d+)\"/;
    ($ng) = $_ =~ /ng\=\"(\d+)\"/;
    ($na) = $_ =~ /na\=\"(\d+)\"/;

    push(@{ $http_sample_list[$row_counter] },$t,$lt,$ts,$s,$lb,$rc,$rm,$tn,$dt,$by,$ng,$na);
    $row_counter++;

  }
}

open (fh,">","$report_dir/sorted_$jmx_log_file.xml") or die "Could not open sorted_$jmx_log_file.xml";
open (sample_fh,">","$report_dir/sample_sorted_$jmx_log_file.xml") or die "Could not open sample_sorted_$jmx_log_file.xml";

@sorted_list = sort { $b->[0] <=> $a->[0] } @http_sample_list;
for (@sorted_list) {
  print fh "<httpSample t=\"$$_[0]\" lt=\"$$_[1]\" ts=\"$$_[2]\" s=\"$$_[3]\" lb=\"$$_[4]\" rc=\"$$_[5]\" rm=\"$$_[6]\" tn=\"$$_[7]\" dt=\"$$_[8]\" by=\"$$_[9]\" ng=\"$$_[10]\" na=\"$$_[11]\"/>\n";
}

@sorted_list2 = sort { $b->[0] <=> $a->[0] } @sample_list;
for (@sorted_list2) {
  if ($$_[4] eq "Next_Command" || $$_[4] eq "Command_Execution" || $$_[4] eq "START" || $$_[4] eq "Debug_Sampler") {}
else {
    print fh "<httpSample t=\"$$_[0]\" lt=\"$$_[1]\" ts=\"$$_[2]\" s=\"$$_[3]\" lb=\"$$_[4]\" rc=\"$$_[5]\" rm=\"$$_[6]\" tn=\"$$_[7]\" dt=\"$$_[8]\" by=\"$$_[9]\" ng=\"$$_[10]\" na=\"$$_[11]\"/>\n";
}
}

close fh;
@sorted_list = sort { $b->[0] <=> $a->[0] } @sample_list;
print sample_fh "<testResults>\n";
for (@sorted_list) {
  print sample_fh "<sample t=\"$$_[0]\" lt=\"$$_[1]\" ts=\"$$_[2]\" s=\"$$_[3]\" lb=\"$$_[4]\" rc=\"$$_[5]\" rm=\"$$_[6]\" tn=\"$$_[7]\" dt=\"$$_[8]\" by=\"$$_[9]\" ng=\"$$_[10]\" na=\"$$_[11]\"/>\n";
}
print sample_fh "</testResults>\n";
close sample_fh;
