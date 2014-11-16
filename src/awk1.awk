BEGIN { RS = "" ; FS = "\n" }
{
if(NF==$1+1)
{
	for(i=1;i<=$1&&NF==$1+1;i++)
	#print NF;	
		system("java Query " $(i+1));
}
else
	print "Invalid Number of Inputs: " (NF-1) " Please Provide " $1 " INPUTS"
}

