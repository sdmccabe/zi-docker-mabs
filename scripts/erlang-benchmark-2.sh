#!/bin/bash

#first, make sure the program is compiled
cd ../models/erlang
erlc agents2.erl
cd ../../scripts/
echo "threads,real,user,sys,memory" > results/experiment-2/erlang-benchmark.csv


declare -a parameters=(1 2 4 5 8 10 16 20 25 32 40 50 80 100 125 200 250 400 500)
for threads in ${parameters[@]}
do
for ((i=0; i<10; i++))
do 
{ /usr/bin/time -f "${threads},%e,%U,%S,%M" erl -pa ../models/erlang/ -run agents2 main $threads 10000000 100000 30 30 -run init stop --noshell ; } 2>> results/experiment-2/erlang-benchmark.csv
done
done
