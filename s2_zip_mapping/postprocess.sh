#!/bin/sh

# combine all of the output
echo 'Combining Output'
cat out_data/*.csv > out_data/s2-cells-by-us-zip.csv

# find the problem zips
echo 'Finding Errors'
grep -e '-' -e 'Exception' out_data/s2-cells-by-us-zip.csv | awk -F"," '{print $5}' | sort -u > out_data/problem-zips.txt

# get rid of the bad records
echo 'Cleaning Output'
grep -v '-' out_data/s2-cells-by-us-zip.csv | grep -v 'Exception' > out_data/s2-cells-by-us-zip-cleaned.csv

# dedupe the file (noting the dupes); also shuffle the file so we randomize the dedupes
echo 'Deduping Output'
awk -F"," 'a[$1]++ == 1 {print $0}' out_data/s2-cells-by-us-zip-cleaned.csv | sort --field-separator=',' -k 1 > out_data/duplicates.txt
shuf out_data/s2-cells-by-us-zip-cleaned.csv | awk -F"," 'a[$1]++ == 0 {print $0}' > out_data/s2-cells-by-us-zip-unique.csv

# note which zips we finished
echo 'Finding Missing Output'
awk -F"," '{print $5}' out_data/s2-cells-by-us-zip-unique.csv | sort -u > out_data/included-zips.txt
comm -23 data/all-zips.txt out_data/included-zips.txt > out_data/missing-zips.txt
cp out_data/missing-zips.txt /scratch/s2/problem-zips.txt

# put the output in Google's format
echo 'Reformatting Output'
awk -F"," '{printf "%s0000000,0_%s-%s-%s\n", $1, $3, $4, $5}' out_data/s2-cells-by-us-zip-unique.csv > out_data/s2-cells-map.csv
gzip out_data/s2-cells-map.csv
