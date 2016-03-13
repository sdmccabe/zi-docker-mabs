#!/bin/bash

#first, make sure the program is compiled
cd /models/scala
sbt clean compile assembly
cd /scripts/
echo "threads,real,user,sys,memory" > results/experiment-2/scala-benchmark.csv


declare -a parameters=(1 2 4 5 8 10 16 20 25 32 40 50 80 100 125 200 250 400 500)
for threads in ${parameters[@]}
do
for ((i=0; i<10; i++))
do 
{ /usr/bin/time -f "${threads}1,%e,%U,%S,%M" java -jar /models/scala/target/scala-2.11/ZIscala-assembly-1.0.jar -n 10000000 -a 100000 -t $threads ; } 2>> results/experiment-2/scala-benchmark.csv
done
done