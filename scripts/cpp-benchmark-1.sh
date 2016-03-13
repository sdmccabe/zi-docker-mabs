#!/bin/bash

#first, make sure the program is compiled
cd /models/cpp
make
cd /scripts/
echo "threads,real,user,sys,memory" > results/experiment-1/cpp-benchmark.csv


declare -a parameters=(1 2 4 5 8 10 16 20 25 32 40 50 80 100 125 200 250 400 500)
for threads in ${parameters[@]}
do
for ((i=0; i<10; i++))
do 
{ /usr/bin/time -f "${threads},%e,%U,%S,%M" /models/cpp/zi-traders -n 1000000 -a 10000 -t $threads ; } 2>> results/experiment-1/cpp-benchmark.csv
done
done