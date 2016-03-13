#!/bin/bash


cd /models/haskell/
cabal update
cabal install parallel
cabal install random
cd /scripts/
echo "threads,real,user,sys,memory" > results/experiment-2/haskell-benchmark.csv

declare -a parameters=(1 2 4 5 8 10 16 20 25 32 40 50 80 100 125 200 250 400 500)
for threads in ${parameters[@]}
do
for ((i=0; i<10; i++))
do 
{ /usr/bin/time -f "${threads},%e,%U,%S,%M" lein run -t $threads ; } 2>> results/experiment-2/haskell-benchmark.csv
done
done