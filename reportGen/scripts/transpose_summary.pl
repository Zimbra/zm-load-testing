#!/usr/bin/perl

my @rows = ();
my @transposed = ();

$report_dir=$ARGV[0];
open F1,"$report_dir/zmstat_charts/summary.csv";
while(<F1>) {
    chomp;
    push @rows, [split /,/ ];
}
#print @rows;

for my $row (@rows) {
  for my $column (0 .. $#{$row}) {
    push(@{$transposed[$column]}, $row->[$column]);
  }
}

for my $new_row (@transposed) {
  for my $new_col (@{$new_row}) {
      print $new_col, ",";
  }
  print "\n";
}
