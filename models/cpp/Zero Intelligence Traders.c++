/*
	Zero Intelligence Traders
	
	Object-Oriented Version+
	
	Robert Axtell
	
	The Brookings Institution
	Middlebury College
		and
  George Mason University
	
	First version: October 1998
	Subsequent version: September 2004
  Current version: July 2009
  Updated for XCode 7 and OS 10.10: Fall 2015
	
	Reference: Gode and Sunder, QJE, 1993

 */

#if defined (_MSC_VER)  // Visual studio
#define thread_local __declspec( thread )
#elif defined (__GCC__) // GCC
#define thread_local __thread
#endif
#include <iostream>
#include <time.h>
#include <thread>
#include "Data.h"
#include <vector>
#include <random>
#include <gflags/gflags.h>

using namespace std;

//////////////////////////////////////////////
//
//	Constant, type and variable definitions...
//
//////////////////////////////////////////////

#define seed 1
#define seedRandomWithTime true
#if seedRandomWithTime
#define theSeed time(0)
#else
#define theSeed seed
#endif

//declare gflags
	DEFINE_int32(a, -1, "Number of buyers and sellers.");
	DEFINE_int32(t, -1, "Number of threads to use.");
	DEFINE_int32(n, -1, "Maximum number of trades.");

	const bool buyer = true;
	const bool seller = false;

// const int maxNumberOfTrades = 5000000;
	int maxNumberOfTrades;
//	Specify the number of agents of each type...
// const int numberOfBuyers = 50000;
// const int numberOfSellers = 50000;
	int numberOfBuyers;
	int numberOfSellers;

//	Specify the maximum internal values...
	const int maxBuyerValue = 30;
	const int maxSellerValue = 30;

// static const int numThreads = 128;
	int numThreads;

//	Define an agent...
	class Agent
	{
		bool buyerOrSeller;
		int quantityHeld;
		int value;
		int price;
	public:
		Agent(bool agentType);
		bool GetBuyerOrSeller();
		int GetQuantityHeld();
		void SetQuantityHeld (int theQuantity);
		int FormBidPrice();
		int FormAskPrice();
		int GetPrice();
		void SetPrice (int thePrice);
	};

	typedef Agent* AgentPtr;

//	Define the model object and declare an instance...
	class Model
	{
	// AgentPtr Buyers[numberOfBuyers];
	// AgentPtr Sellers[numberOfSellers];

		std::vector<AgentPtr> Buyers;
		std::vector<AgentPtr> Sellers;
	public:
		Model();
		Data TradeData, PriceData;
		void DoTrades (int maxTrades);
		void DoTrading(int& deltaTime1, int& deltaTime2);
	};

	

	//replacing the RNG
int IntegerInRange(int min, int max) {
    static thread_local std::mt19937* generator = nullptr;
    if (!generator) generator = new std::mt19937(clock() + std::hash<std::thread::id>()(std::this_thread::get_id()));
    std::uniform_int_distribution<int> distribution(min, max);
    return distribution(*generator);
}

/////////////////////////////
//
//	Object Implementations...
//
/////////////////////////////

	Agent::Agent (bool agentType)
	{
		if (agentType == buyer)
		{
			buyerOrSeller = buyer;
			quantityHeld = 0;
			value = IntegerInRange(1, maxBuyerValue);
		}
	else	// agentType == seller
	{
		buyerOrSeller = seller;
		quantityHeld = 1;
		value = IntegerInRange(1, maxSellerValue);
	};
};	//	Agent::Agent()

bool Agent::GetBuyerOrSeller()
{
	return buyerOrSeller;
};	//	Agent:GetBuyerOrSeller()

int Agent::GetQuantityHeld()
{
	return quantityHeld;
};	//	Agent::GetQuantityHeld()

void Agent::SetQuantityHeld (int theQuantity)
{
	quantityHeld = theQuantity;
};	//	Agent::SetQuantityHeld()

int Agent::FormBidPrice()
{
	return IntegerInRange(1, value);
};	//	Agent::FormBidPrice()

int Agent::FormAskPrice()
{
	return IntegerInRange(value, maxBuyerValue);
};	//	Agent::FormAskPrice()

int Agent::GetPrice()
{
	return price;
};	//	Agent::GetPrice()

void Agent::SetPrice (int thePrice)
{
	price = thePrice;
};	//	Agent::SetPrice()

Model::Model()
{
//	Fill the agent fields...
	//cout << "begin alloc" << endl;
 	//Buyers = new Agent[numberOfBuyers]{true};
 	//Sellers = new Agent[numberOfSellers]{false};
	for (int i = 0; i < numberOfBuyers; ++i) {
		Buyers.push_back(new Agent(buyer));
	}
	for (int i = 0; i < numberOfSellers; ++i) {
		Sellers.push_back(new Agent(seller));
	}
	//cout << "end alloc" << endl;
 	//	First the buyers...
	for (int i=0; i<numberOfBuyers; i=i+1)
		Buyers[i] = new Agent(buyer);

	//	Now the sellers...
	for (int i=0; i<numberOfSellers; i=i+1)
		Sellers[i] = new Agent(seller);

//	Now initialize the data objects...
	PriceData.Init();
	TradeData.Init();

};	//	Model::Model()

void Model::DoTrades (int maxTrades)
//
//	This method pairs agents at random and then selects a price randomly...
//
{
	int buyerIndex, sellerIndex;
	int bidPrice, askPrice, transactionPrice;
	
	for (int i=1; i<=maxTrades; i=i+1)
	{
	 	//	Pick a buyer at random, then pick a 'bid' price randomly between 1 and the agent's private value;
    //
		buyerIndex = IntegerInRange(0, numberOfBuyers-1);
		bidPrice = Buyers[buyerIndex]->FormBidPrice();

	 	//	Pick a seller at random, then pick an 'ask' price randomly between the agent's private value and maxSellerValue;
    //
		sellerIndex = IntegerInRange(0, numberOfSellers-1);
		askPrice = Sellers[sellerIndex]->FormAskPrice();

	 	//	Let's see if a deal can be made...
    //
		if ((Buyers[buyerIndex]->GetQuantityHeld() == 0) && (Sellers[sellerIndex]->GetQuantityHeld() == 1) && (bidPrice >= askPrice))
		{
	 		//	First, compute the transaction price...
      //
			transactionPrice = IntegerInRange(askPrice, bidPrice);
			Buyers[buyerIndex]->SetPrice(transactionPrice);
			Sellers[sellerIndex]->SetPrice(transactionPrice);
			PriceData.AddDatuum(transactionPrice);

	 		//	Then execute the exchange...
      //
			Buyers[buyerIndex]->SetQuantityHeld(1);
			Sellers[sellerIndex]->SetQuantityHeld(0);
			TradeData.AddDatuum(1);
		};
	};
};	//	Model::DoTrades()

void do_join(std::thread& t)
{
    t.join();
}

void Model::DoTrading(int& deltaTime1, int& deltaTime2)
{
	time_t startTime1, startTime2, endTime1, endTime2;
	std::vector<thread> t;
  //thread *t = new thread[numThreads];
	//cout << "starting time" << endl;
	time(&startTime1);
	startTime2 = clock();
	//cout << "forking" << endl;
	for(int i = 0; i < numThreads; i++) {
    //t[i] = thread(&Model::DoTrades, this, maxNumberOfTrades/numThreads);
		t.push_back(thread(&Model::DoTrades, this, maxNumberOfTrades/numThreads));
	}
	//cout <<"joining" << endl;
	for(int i = 0; i < numThreads; i++) {
		t[i].join();
	}
	//std::for_each(t.begin(),t.end(),do_join);
	//cout << "joined" << endl;
	time(&endTime1);
	endTime2 = clock();

	deltaTime1 = difftime(endTime1, startTime1);
	deltaTime2 = endTime2 - startTime2;

};	//	Model::DoTrading()

///////////
//
//	MAIN...
//
///////////
void parseFlags(int argc, char* argv[]) {
	const std::string usage = "An agent-based model of bilateral exchange. Usage:\nzi-traders";
	//usage += argv[0];
	google::SetUsageMessage(usage);
	google::ParseCommandLineFlags(&argc, &argv, true);

	if(FLAGS_a == -1 || FLAGS_n == -1 || FLAGS_t == -1) {
		cout << usage << endl;
		exit(1);
	}
	maxNumberOfTrades = FLAGS_n;
	numberOfBuyers = FLAGS_a;
	numberOfSellers = FLAGS_a;
	numThreads = FLAGS_t;
}
int main(int argc, char* argv[])
{
	int wallTime, CPUtime;
	parseFlags(argc, argv);
	cout << "\nZERO INTELLIGENCE TRADERS" << endl << endl;

	Model ZITraders;
	//cout << "Initalized model" << endl;
	ZITraders.DoTrading(wallTime, CPUtime);
	//cout << "finished trading" <<endl;
	cout << "The model took " << wallTime << " seconds to execute using "
	<< (double) CPUtime/CLOCKS_PER_SEC << " seconds on the cores" << endl;
	cout << "Quantity traded = " << ZITraders.TradeData.GetN() << endl << endl;
	cout << "The average price = " << ZITraders.PriceData.GetAverage() << " and the s.d. is " << ZITraders.PriceData.GetStdDev() << endl;
}
