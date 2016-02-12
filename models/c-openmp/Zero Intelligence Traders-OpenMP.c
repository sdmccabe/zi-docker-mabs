/*
	Zero Intelligence Traders
	
	Non-Object-Oriented Version
	
	Robert Axtell
	
	The Brookings Institution
		and
	George Mason University
	
	First version: October 1998
	Subsequent version: September 2004
  New version: July 2009
	
	Reference: Gode and Sunder, QJE, 1993

 */

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>


//#include <omp.h>

//////////////////////////////////////////////
//
//	Constant, type and variable definitions...
//
//////////////////////////////////////////////

#define false 0
#define true 1

#define seedRandomWithDate true	//	if false, seed the generator with 'seed'
#define seed 1

	int buyer = true;
	int seller = false;

	int MaxNumberOfTrades = 100000000;

//	Specify the number of agents of each type...
	int numberOfBuyers  = 1200000;
	int numberOfSellers = 1200000;

//	Specify the maximum internal values...
	int maxBuyerValue  = 30;
	int maxSellerValue  = 30;

	int numThreads = 32;
//2400

//	Define an agent...
	typedef struct TraderAgent
	{
		int buyerOrSeller;
		int quantityHeld;
		int value;
		int price;
	} Agent;

//	Declare the agent populations...
	Agent *Buyers;
	Agent *Sellers;

	int agentsPerThread;
	int tradesPerThread;
	unsigned int *seeds;


/////////////////
//
//	Procedures...
//
/////////////////

	void InitializeMiscellaneous()
//
//	The only thing accomplished here is to initialize the random number generator
//
	{
		if (seedRandomWithDate)
			srand(time(NULL));
		else
			srand(seed);
};	//	InitializeMiscellaneous()

void InitializeAgents()
//
//	Fill the agent fields...
//
{	
	Buyers = (Agent*)malloc(numberOfBuyers * sizeof(Agent));
	Sellers = (Agent*)malloc(numberOfSellers * sizeof(Agent));
	seeds = (unsigned int*)malloc(numThreads * sizeof(unsigned int));
	int i;

 	//	First the buyers...
	for (i=0; i<numberOfBuyers; i=i+1)
	{
		Buyers[i].buyerOrSeller = buyer;
		Buyers[i].quantityHeld = 0;
		Buyers[i].value = (rand() % maxBuyerValue) + 1;
	};

	//	Now the sellers...
	for (i=0; i<numberOfSellers; i=i+1)
	{
		Sellers[i].buyerOrSeller = seller;
		Sellers[i].quantityHeld = 1;
		Sellers[i].value = (rand() % maxSellerValue) + 1;
	};
	for (i=0; i<numThreads; i=i+1)
	{
		seeds[i] = rand();
	}
};	//	InitializeAgents()

void DoTrades(int threadNum)
//
//	This function pairs agents at random and then selects a price randomly...
//
{
	int i, buyerIndex, sellerIndex;
	int bidPrice, askPrice, transactionPrice;
	
	int lowerBuyerBound, upperBuyerBound, lowerSellerBound, upperSellerBound;
	
	if (numThreads <= 10)
		printf("Thread %i up and running\n", threadNum);

	lowerBuyerBound = threadNum * agentsPerThread;
	upperBuyerBound = (threadNum + 1) * agentsPerThread - 1;
	lowerSellerBound = threadNum * agentsPerThread;
	upperSellerBound = (threadNum + 1) * agentsPerThread - 1;	
	
	for (i=1; i<=tradesPerThread; i++)
	{
	 	//	Pick a buyer at random, then pick a 'bid' price randomly between 1 and the agent's private value;
		buyerIndex = lowerBuyerBound + rand_r(&seeds[threadNum]) % (upperBuyerBound - lowerBuyerBound);
		bidPrice = (rand_r(&seeds[threadNum]) % Buyers[buyerIndex].value) + 1;

	 	//	Pick a seller at random, then pick an 'ask' price randomly between the agent's private value and maxSellerValue;
		sellerIndex = lowerSellerBound + rand_r(&seeds[threadNum]) % (upperSellerBound - lowerSellerBound);
		askPrice = Sellers[sellerIndex].value + (rand_r(&seeds[threadNum]) % (maxSellerValue - Sellers[sellerIndex].value + 1));

	 	//	Let's see if a deal can be made...
	 	//
		if ((Buyers[buyerIndex].quantityHeld == 0) && (Sellers[sellerIndex].quantityHeld == 1) && (bidPrice >= askPrice))
		{
	 		//	First, compute the transaction price...
			transactionPrice = askPrice + rand_r(&seeds[threadNum]) % (bidPrice - askPrice + 1);
			Buyers[buyerIndex].price = transactionPrice;
			Sellers[sellerIndex].price = transactionPrice;

	 		//	Then execute the exchange...
			Buyers[buyerIndex].quantityHeld = 1;
			Sellers[sellerIndex].quantityHeld = 0;
		};
	};
};	//	DoTrades()

void ComputeStatistics(clock_t elapsedTime)
//
//	Determine the total quantities bought and sold...
//	...as well as statistics about prices
//
{
	int i;
	int numberBought = 0;
	int numberSold= 0;
	int sum = 0;
	double sum2 = 0.0;
	int N = 0;
	double avgPrice, sd;
	
	//	First, compute the quantity purchased...
	for (i=0; i<numberOfBuyers; i++)
		if (Buyers[i].quantityHeld == 1)
			numberBought++;
//	printf("\nQuantity bought = %i", numberBought);

	//	Next, get the quantity sold...
		for (i=0; i<numberOfSellers; i++)
			if (Sellers[i].quantityHeld == 0)
				numberSold++;
//	printf("; quantity sold = %i\n", numberSold);

	//	Now let's compute the average price paid as well as the standard deviation...
			for (i=0; i<numberOfBuyers; i++)
				if (Buyers[i].quantityHeld == 1)
				{
					sum += Buyers[i].price;
					sum2 += pow(Buyers[i].price, 2);
					N++;
				};
				for (i=0; i<numberOfSellers; i++)
					if (Sellers[i].quantityHeld == 0)
					{
						sum += Sellers[i].price;
						sum2 += pow(Sellers[i].price, 2);
						N++;
					};
					avgPrice = (double) sum / (double) N;
					sd = sqrt((sum2 - (double) N * pow(avgPrice, 2)) / (double) (N - 1));
					printf("%i items bought and %i items sold\n", numberBought, numberSold);
					printf("The average price = %f and the s.d. is %f\n", avgPrice, sd);
					printf("The execution time was %f seconds\n", (double) elapsedTime/1000000.0);
};	//	ComputeStatistics()

void OpenMarket()
{
	clock_t startTime1, endTime1;
	time_t startTime2, endTime2;
	
	startTime1 = clock();
	time(&startTime2);
	
	int i;

#pragma omp parallel num_threads(numThreads) private(i)
	{
    //srand(int(time(NULL)) ^ omp_get_thread_num()); 
    //srand((int) time(NULL) ^ omp_get_thread_num()); // maybe this goes below
//    printf("Max threads: %i", omp_get_max_threads());
//    omp_set_num_threads(numThreads);
		#pragma omp for
		for (i=0; i<numThreads; i++)
			DoTrades(i);
	}
	endTime1 = clock();
	time(&endTime2);
	
	ComputeStatistics(endTime1 - startTime1);
	endTime2 = (endTime2 - startTime2);
	printf("Wall time: %d seconds\n", (int) endTime2);
};

///////////
//
//	MAIN...
//
///////////
void print_usage() {
	printf("Usage: zi-traders -a num-agents -n max-trades -t num-threads\n");
}

void parseFlags (int argc, char *argv[]){
	numberOfBuyers = -1;
	numberOfSellers = -1;
	numThreads = -1;
	MaxNumberOfTrades = -1;
	int option = 0;
	while ((option = getopt(argc, argv,"a:n:t:")) != -1) {
		switch (option) {
			case 'n' : MaxNumberOfTrades = atoi(optarg);
			break;
			case 'a' : numberOfBuyers = atoi(optarg); 
			break;
			case 't' : numThreads = atoi(optarg);
			break;
			default: print_usage(); 
			exit(EXIT_FAILURE);
		}
	}
	if (MaxNumberOfTrades == -1 || numberOfBuyers == -1 || numThreads == -1) {
		print_usage();
		exit(EXIT_FAILURE);
	}
	numberOfSellers = numberOfBuyers;
}
int main(int argc, char* argv[])
{
	parseFlags(argc, argv);

	agentsPerThread = numberOfBuyers/numThreads;
	tradesPerThread = MaxNumberOfTrades/numThreads;


	printf("\nZERO INTELLIGENCE TRADERS\n");

	InitializeMiscellaneous();
	
	InitializeAgents();

	
	OpenMarket();
	
	return(0);
}