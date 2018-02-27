#!/bin/sh
echo 100000
python s2cellmapping.py 95453 100000 99999.csv
echo 90000
python s2cellmapping.py 80000 90000 90000.csv
echo 80000
python s2cellmapping.py 70000 80000 80000.csv
echo 70000
python s2cellmapping.py 60000 70000 70000.csv
echo 60000
python s2cellmapping.py 50000 60000 60000.csv
echo done
