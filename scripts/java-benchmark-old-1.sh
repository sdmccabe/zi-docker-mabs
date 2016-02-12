#!/bin/bash

#first, make sure the program is compiled
cd /models/java
rm -R build/
mkdir build
mkdir build/META-INF
echo "Main-Class: ZITraders" > build/META-INF/MANIFEST.MF
javac -d ./build *.java
cd build
jar cvmf META-INF/MANIFEST.MF ZItraders.jar *
cd /scripts/
echo "threads,real,user,sys,memory" > results/experiment-1/java-old-benchmark.csv


declare -a parameters=(1 2 4 5 8 10 16 20 25 32 40 50 80 100 125 200 250 400 500)
for threads in ${parameters[@]}
do
for ((i=1; i<10; i++))
do 
{ /usr/bin/time -f "${threads},%e,%U,%S,%M" java -jar /models/java/build/ZItraders.jar 10000 1000000 $threads old; } 2>> results/experiment-1/java-old-benchmark.csv
done
done