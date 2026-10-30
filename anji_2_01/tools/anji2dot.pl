#!/usr/bin/perl

# crude XML parser to generate dot plots from ANJI chromosomes

if ($#ARGV != 0) {
	print <<EOF;
usage: anji2dot.pl <filename>
EOF
	exit;
}

open(FP,$ARGV[0]);

# count per neuron type
$ix = 0;
$hx = 0;
$ox = 0;

my $str = "";

while (<FP>) {
	chop;
	$str = $str.$_;
}	
close(FP);

$str =~ s/>//g;
$str =~ s/\///g;
@entries = split(/</,$str);

print "digraph G {\n\nrankdir=BT;";

$inp_nodes,$hid_nodes,$out_nodes;
$inp_nodes=$hid_nodes=$out_nodes="{rank = same; ";

foreach $entry (@entries) {

  if ($entry!~/chromosome/) {
	parse($entry);
  }

}

print "$inp_nodes }\n";
#print "$hid_nodes }\n";
print "$out_nodes }\n";
#print "Input [label=\"Input\"]; Hidden [label=\"Hidden\"]; Output [label=\"Hidden\"];\n";
#print "Input -> Hidden -> Output;\n";

print "}\n\n";

############################################################################
# subroutines
############################################################################
sub parse {

  my ($str) = @_;

  if (length($str)<=0) {
	return;
  }
  $str =~ s/\"//g;

  my @fields = split(/[\s]+/,$str);

  my $id, $src, $dest, $w, $ntype, $field;


  
  $type = shift(@fields);

  foreach $field (@fields) {
	# print "[$field] ";
	my @varval = split(/\=/,$field);
	if ($varval[0] eq "id" && $type=="neuron") { $id = $varval[1]; }	
	if ($varval[0] eq "type") { $ntype = $varval[1]; }	
	if ($varval[0] eq "src-id") { $src = $varval[1]; }	
	if ($varval[0] eq "dest-id") { $dest = $varval[1]; }	
	if ($varval[0] eq "weight") { $w = $varval[1]*10.0; }	
  }

  if ($type eq "neuron") {
	my $bb;

	if ($ntype eq "in") {
		$bb = "fillcolor=\"red\" style=\"filled\" shape=\"box\"";
		$pos = "pos=\"$ix,6.5!\"";
		$ix = $ix + 1.5;
		$inp_nodes = $inp_nodes." \"n$id\"; ";
	} elsif ($ntype eq "hid") {
		$bb = "fillcolor=\"orange\" style=\"filled\" shape=\"hexagon\" ";
		$pos = "pos=\"$hx,3.5!\"";
		$hx = $hx + 1.5;
		$hid_nodes = $hid_nodes." \"n$id\"; ";
	}
	elsif ($ntype eq "out") {
		$bb = "fillcolor=\"green\" style=\"filled\"";
		$pos = "pos=\"$ox,0.5!\"";
		$ox = $ox + 1.5;
		$out_nodes = $out_nodes." \"n$id\"; ";
	}
	
	print "  n$id [shape=\"circle\", label=\"$ntype: $id\", $bb, $pos]\n";
  } elsif ($type eq "connection") {
	# print "  n$src -> n$dest [weight=$w]; \n";
	$style = ($w>0)?"[style=bold, arrowsize=2.0, arrowhead=normal]":"[style=dashed, arrowsize=1.0, arrowhead=dot]";
	print "  n$src -> n$dest $style; \n";
  }

}
