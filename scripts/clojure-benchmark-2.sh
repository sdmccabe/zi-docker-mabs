#!/bin/bash

#first, make sure the program is compiled
cd /models/clojure/zi-traders
lein deps
lein compile
lein uberjar
cd /scripts/
echo "threads,real,user,sys,memory" > results/experiment-2/clojure-benchmark.csv


declare -a parameters=(1 2 4 5 8 10 16 20 25 32 40 50 80 100 125 200 250 400 500)
for threads in ${parameters[@]}
do
for ((i=1; i<10; i++))
do 
{ /usr/bin/time -f "${threads},%e,%U,%S,%M" java -jar /models/clojure/zi-traders/zi-traders-0.1.0-SNAPSHOT-standalone.jar -n 10000000 -a 100000 -t $threads ; } 2>> results/experiment-2/clojure-benchmark.csv
done
done
