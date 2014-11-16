To compile the source code and generate index dump

Run index.sh file from bin folder with two arguments
	1. File - To be parsed for creating index
	2. Path - To store index dump
	e.g., bash index.sh ~/sample.xml index

Run query.sh to search an index
	This script takes input from standard input with multiple lines
	1. First line gives number of queries
	2. Search query in next lines(given no of queries), each search query can have multiple terms/field specific terms
	3. Fields can be searched as plain text or specific to "title", "body", "catogery"
	
	e.g., printf "2\n b:german\n carmony indiana"|bash query.sh

	4. b:<word> searches for this word in body context, t:carmony<word> searches word in titles, c:<word> searches this word in catogeries 
	5. plain text will be searched in all indexes
	6. Print empty string if no matches found
	7. Empty line is usedto seperate results when multiple queries are passed as input


Implementation:


I  have written own approach which ranks the documents and displays documents based on rank, giving priority in decreasing order of title, infobox, catogeries, body, external links.

1. Inverted File is generated and is sorted according to term, alphabetically.
2. Created Secondary index which saves term and offset of this term in primary index.
3. While searching each term is given to a thread, which makes read operation on index files. This thread finds nearest word in seconday index, extracts possible two offsets in which term could be. THis is done using binary search on secondary index. 
4. Term is search in b/w offsets and final posting is loaded into cache.
5. After retrieving postings, I am using my own defined ranking appraoch and displaying top ten documents.
6. Able to handle titles and single words more efficiently.


Thanks,
Spandan
 


