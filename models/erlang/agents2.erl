-module(agents2).
-compile(export_all).
-import(rand, [uniform/1]).
%-export([zi_threads/5, run/5, main/1, wait_for_n_tasks/1,run_task_and_notify_completion/7]).
%% create individual sellers and buyers with sell costs and buy values
seller(MaxSell,N) -> 
	Cost = rand:uniform(MaxSell-1),
	Type = seller,
	Id = N,
	{Type,Cost,Id}.

buyer(MaxBuy,N) ->
	Value = rand:uniform(MaxBuy-1),
	Type = buyer,
	Id = N,
	{Type,Value,Id}. 

%% create a list of N buyers and N sellers with MaxBuyValue and MaxSellerCost
create_sellers(N,MaxSell) -> create_sellers(N,MaxSell,[]). 
create_sellers(0,MaxSell, Acc) -> Acc;
create_sellers(N,MaxSell,Acc) -> create_sellers(N-1,MaxSell,[seller(MaxSell,N)|Acc]).

create_buyers(N,MaxBuy,Offset) -> create_buyers(N,MaxBuy,Offset,[]).
create_buyers(0,MaxBuy,Offset,Acc) -> Acc;
create_buyers(N,MaxBuy,Offset,Acc) -> create_buyers(N-1,MaxBuy,Offset,[buyer(MaxBuy,N+Offset)|Acc]).

%%initializes the order book
init_order_book(MaxSell) -> [{0,-1,false}, {MaxSell,-1,false}].

%%updates the order book based on whether its a bid or and ask
update_order_book(buyer,X,[H|T]) -> [{X,true}|T];	
update_order_book(_,X,[H|T]) -> [H|{X,true}].


form_price({seller,Cost,_}, MaxBuy) -> {rand:uniform(MaxBuy-Cost)+Cost,ask};
form_price({buyer,Value,_}, _) -> {rand:uniform(Value), bid}.

sum(List) -> sum(List,0).
sum([],Acc) -> Acc;
sum([H|T],Acc) -> sum(T,Acc+H).	

mean(List) -> sum(List)/length(List).


do_trade(0,Agents,[BuyOrder,SellOrder],Transactions,MaxBuy,MaxSell) ->
	%io:format("Done trading~n"),
	NumTransactions = length(Transactions),
	AvgPrice = mean(Transactions),
	io:format("Number of Transaction: ~w Average Price: ~w~n",				[NumTransactions,AvgPrice]);
	%io:format("~w~n",[erlang:timestamp()]);
do_trade(NumSteps,Agents,[BuyOrder,SellOrder],Transactions,MaxBuy,MaxSell) ->
	%io:format("Trade number ~w ~n", [NumSteps]),
	%erlang:display(NumSteps),
	Pos = rand:uniform(length(Agents)),
	Agent = lists:nth(Pos,Agents),
	{Price, Type} = form_price(Agent,MaxBuy),

	NewBestAsk = case Type =:= ask of 
			true ->  if Price =< element(1,SellOrder) -> Price;
					Price > element(1,SellOrder) -> 							element(1,SellOrder)
					end;
			false -> element(1,SellOrder)
			end,	
	BestAskId = case Type =:= ask of 
			true ->  if Price =< element(1,SellOrder) -> element(3,Agent);
					Price > element(1,SellOrder) -> 							element(2,SellOrder)
					end;
			false -> element(2,SellOrder)
			end,
	AskIn = case Type =:= ask of 
			true -> if Price =< element(1,SellOrder) -> true;
					Price > element(1,SellOrder) -> element(3,SellOrder)
					end;
			false -> element(3,SellOrder)
			end,
	NewBestBid = case Type =:= bid of 
			true -> if Price >= element(1,BuyOrder) -> Price;
					Price < element(1,BuyOrder) -> 								element(1,BuyOrder)
					end;
			false -> element(1,BuyOrder) 
			end,
	BestBidId = case Type =:= bid of 
			true ->	if Price >= element(1,BuyOrder) -> element(3,Agent);
					Price < element(1,BuyOrder) -> 								element(2,BuyOrder)
					end;
			false -> element(2,BuyOrder)
			end,
	BidIn = case Type =:= bid of 
			true ->	if Price >= element(1,BuyOrder) -> true;
					Price < element(1,BuyOrder) -> element(3,BuyOrder)
					end;
			false -> element(3,BuyOrder)
			end,

	case (NewBestAsk =< NewBestBid) and AskIn and BidIn of
		true ->
			%io:fwrite("Trade made, book: ~w, Transactions: ~w, Traders ~w~n,",			%[init_order_book(MaxSell),[element(1,SellOrder) | Transactions],			%[BestBidId | [BestAskId | Traded]]]),
			do_trade(NumSteps-1,Agents,init_order_book(MaxSell),					[element(1,SellOrder) | Transactions],MaxBuy, MaxSell);
		false ->
			%io:fwrite("No trade, book: ~w, Transactions: ~w, Traders: ~w~n",			%[[{NewBestBid, BestBidId,BidIn},{NewBestAsk,BestAskId,AskIn}], 			%Transactions,Traded]),
			do_trade(NumSteps-1,Agents,[{NewBestBid, BestBidId,BidIn},				{NewBestAsk,BestAskId,AskIn}],Transactions,MaxBuy, MaxSell)

			
	end.



run(NumSteps,NumSellers,NumBuyers,MaxSell,MaxBuy) ->
		Sellers = create_sellers(NumSellers,MaxSell),
		Buyers = create_buyers(NumBuyers,MaxBuy,NumSellers),
		Agents = lists:merge(Sellers,Buyers),
		Transactions = [],
		OrderBook = init_order_book(MaxSell),
		do_trade(NumSteps,Agents,OrderBook,Transactions,MaxBuy,MaxSell).


zi_threads(0,NumSteps,NumAgents,MaxSell,MaxBuy) ->
	done;
zi_threads(NumThreads,NumSteps,NumAgents,MaxSell,MaxBuy) ->
	%io:format("~w~n",[erlang:timestamp()]),
	spawn(agents2,run,[NumSteps,NumAgents,NumAgents,MaxSell,MaxBuy]),
	zi_threads(NumThreads-1,NumSteps,NumAgents,MaxSell,MaxBuy).

main(Args) ->
 io:fwrite("Args"),
 Par = list_to_tuple(Args),
 ThreadsNumeric = list_to_integer(element(1, Par)),
 StepsNumeric = list_to_integer(element(2, Par)),
 AgentsNumeric = list_to_integer(element(3, Par)),
 SellNumeric = list_to_integer(element(4, Par)),
 BuyNumeric = list_to_integer(element(5, Par)),
 io:format("Threads: ~w~n", [ThreadsNumeric]),
 io:format("Steps: ~w~n", [StepsNumeric]),
 io:format("Agents: ~w~n", [AgentsNumeric]),
 io:format("Sell: ~w~n", [SellNumeric]),
 io:format("Buy: ~w~n", [BuyNumeric]),
 Self = self(),
 Task = fun (I) -> spawn(?MODULE, run_task_and_notify_completion, [Self, fun run/5, StepsNumeric div ThreadsNumeric, AgentsNumeric div ThreadsNumeric, AgentsNumeric div ThreadsNumeric, SellNumeric, BuyNumeric]) end,
lists:foreach(Task, lists:seq(1, ThreadsNumeric)),
wait_for_n_tasks(ThreadsNumeric).
%[StepsNumeric, AgentsNumeric, SellNumeric, BuyNumeric]

% Lots of code taken from
% http://www.programming-idioms.org/idiom/56/launch-1000-parallel-tasks-and-wait-for-completion/789/haskell

run_task_and_notify_completion(ParentPid, Fun, A, B, C, D, E) ->
    Fun(A,B,C,D,E),
    ParentPid ! done.

wait_for_n_tasks(0) ->
    io:format("Finished~n");
wait_for_n_tasks(N) when N > 0 ->
    receive
        done ->
            ok
    end,
    wait_for_n_tasks(N-1).

