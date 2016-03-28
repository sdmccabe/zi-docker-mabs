from __future__ import division
from buyeragent import BuyerAgent
from selleragent import SellerAgent
from data import Data
import random
from time import time
import threading as mt
from threading import Lock
import gflags
import sys

lock = Lock()
FLAGS = gflags.FLAGS
gflags.DEFINE_integer('a', None, 'number of buyers and sellers', lower_bound = 0)
gflags.DEFINE_integer('t', None, 'number of threads', lower_bound = 0)
gflags.DEFINE_integer('n', None, 'number of trades (100 * agents a good value)', lower_bound = 0)


class ZITraders:

    def __init__(self, numagents, numtrades):
        self.verbose_output = False
        number_of_buyers = numagents
        number_of_sellers = numagents
        max_buyer_value = 30.0
        max_seller_cost = 30.0
        self.max_number_of_trades = numtrades

        self.buyers = [BuyerAgent(max_buyer_value)
                       for _ in xrange(number_of_buyers)]

        self.sellers = [SellerAgent(max_seller_cost)
                        for _ in xrange(number_of_sellers)]

        self.transactions = Data()

    def do_trading(self, numthreads):
        cpu_count = numthreads
        results = [[]] * cpu_count
        threads = []
        for t in range(cpu_count):
            print t
            threads.append(mt.Thread(target=do_trading_chunk, args=[int(
                self.max_number_of_trades / cpu_count),
                self.buyers, self.sellers, results, t]))
            threads[-1].start()
        for thread in threads:
            print thread
            thread.join()

        transactions = Data()
        transactions.consolidate(results)

        print("\nFinal stats: %i transactions at %2.2f average price; "
              "%2.2f standard deviation.\n"
              % (transactions.N,
                 transactions.get_average(),
                 transactions.get_std_dev()))


def do_trading_chunk(number_of_trades, buyers, sellers, results, i):
    print 'here', i
    random.seed()
    transactions = Data()
    for counter in xrange(number_of_trades):
        if counter % 100000 == 0:
            print counter
        buyer = random.choice(buyers)
        seller = random.choice(sellers)
        lock.acquire()
        buyer.acquire()
        seller.acquire()
        lock.release()

        bid_price = buyer.form_bid_price()
        ask_price = seller.form_ask_price()

        if not buyer.traded and not seller.traded and bid_price > ask_price:
            transaction_price = (ask_price
                                 + random.random()
                                 * (bid_price - ask_price))
            buyer.traded = True
            buyer.price = transaction_price
            seller.traded = True
            seller.price = transaction_price

            transactions.add_datuum(transaction_price)
        buyer.release()
        seller.release()

    #         if self.verbose_output:
    #             print("Found two agents willing to trade: ")
    #             print("Price of %2.2f with buyer's bid of %2.2f "
    #                   "and seller asking price of %2.2f"
    #                   % (transaction_price, bid_price, ask_price))
    print transactions.N
    results[i] = transactions


if __name__ == '__main__':
    try:
        argv = FLAGS(sys.argv)  # parse flags
    except gflags.FlagsError as e:
        print('%s\\nUsage: %s ARGS\\n%s' % (e, sys.argv[0], FLAGS))
        sys.exit(1)

    t = time()
    simulation = ZITraders(FLAGS.a, FLAGS.n)
    simulation.do_trading(FLAGS.t)
    print('time %2.2f' % (time() - t))
