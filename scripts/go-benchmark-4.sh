#!/bin/bash

#first, make sure the program is compiled
cd /models/go
go get github.com/grd/stat
go get github.com/pkg/profile
go build -o zi-traders zi-traders.go
cd /scripts/
echo "threads,real,user,sys,memory" > results/experiment-4/go-benchmark.csv


declare -a parameters=(1 2 4 5 8 10 16 20 25 32 40 50 80 100 125 200 250 400 500)
for threads in ${parameters[@]}
do
for ((i=0; i<10; i++))
do 
{ /usr/bin/time -f "${threads},%e,%U,%S,%M" /models/go/zi-traders -n=1000000000 -a=10000000 -t=$threads ; } 2>> results/experiment-4/go-benchmark.csv
done
done