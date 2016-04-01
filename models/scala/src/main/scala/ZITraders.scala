package ZIMarketParallel

import java.util.Calendar
import java.util.Random
import java.text.DecimalFormat
import com.mosesn.pirate.Pirate
import java.util.concurrent._

object ZITraders {

   def main(commandLineArgs: Array[String]) {
    val args = Pirate("[ -t int -a int -n int]")(commandLineArgs)
    val t : Option[Int] = args.intMap.get('t')
    val a : Option[Int] = args.intMap.get('a')
    val n : Option[Int] = args.intMap.get('n')

    //println(t.getOrElse(1))

    val Market = new ZITraders(t.getOrElse(1), a.getOrElse(1000), n.getOrElse(10000))
    //if (commandLineArgs.length == 0) {
      Market.doNew = false
      //Market.numberOfThreads = t.getOrElse(1)
    //}
    Market.OpenTrading()
  }
}

class ZITraders private (threads: Int, agents: Int, trades: Int) {

  val verboseOutput = false

  val numberOfBuyers = agents

  val numberOfSellers = agents

  val maxBuyerValue = 30.0

  val maxSellerCost = 30.0

  val maxNumberOfTrades = trades

  val threadedTrades = true

  val numberOfSubpopulations = 1

  val numberOfThreads = threads

  val sleepDuration = 10

  var doNew: Boolean = true

  var twoDigits: DecimalFormat = new DecimalFormat("0.00")

  var people: AgentPopulation = _

  var Transactions = new Array[Data](numberOfThreads)

  try {
    people = new AgentPopulation(numberOfBuyers, maxBuyerValue, numberOfSellers, maxSellerCost)
    for (i <- 0 until numberOfThreads) {
      Transactions(i) = new Data()
    }
  } catch {
    case e: Exception => e.printStackTrace()
  }

  def DoSingleThreadedTrades() {
    var tradingRun: DoTrades = null
    numberOfSubpopulations match {
      case 1 => 
        tradingRun = new DoTrades(0, maxNumberOfTrades, 1, numberOfBuyers, 1, numberOfSellers)
        tradingRun.run()

      case 2 => 
        tradingRun = new DoTrades(0, maxNumberOfTrades / 4, 1, numberOfBuyers / 2, numberOfSellers / 2, 
          numberOfSellers)
        tradingRun.run()
        tradingRun = new DoTrades(0, maxNumberOfTrades / 4, numberOfBuyers / 2, numberOfBuyers, 1, numberOfSellers / 2)
        tradingRun.run()
        tradingRun = new DoTrades(0, maxNumberOfTrades / 4, 1, numberOfBuyers / 2, 1, numberOfSellers / 2)
        tradingRun.run()
        tradingRun = new DoTrades(0, maxNumberOfTrades / 4, numberOfBuyers / 2, numberOfBuyers, numberOfSellers / 2, 
          numberOfSellers)
        tradingRun.run()

      case 3 => 
        tradingRun = new DoTrades(0, maxNumberOfTrades / 3, 1, numberOfBuyers / 3, 2 * numberOfSellers / 3, 
          numberOfSellers)
        tradingRun.run()
        tradingRun = new DoTrades(0, maxNumberOfTrades / 3, numberOfBuyers / 3, 2 * numberOfBuyers / 3, 
          numberOfSellers / 3, 2 * numberOfSellers / 3)
        tradingRun.run()
        tradingRun = new DoTrades(0, maxNumberOfTrades / 3, 2 * numberOfBuyers / 3, numberOfBuyers, 1, 
          numberOfSellers / 3)
        tradingRun.run()

      case 4 => 
        tradingRun = new DoTrades(0, maxNumberOfTrades / 4, 1, numberOfBuyers / 4, 3 * numberOfSellers / 4, 
          numberOfSellers)
        tradingRun.run()
        tradingRun = new DoTrades(0, maxNumberOfTrades / 4, numberOfBuyers / 4, numberOfBuyers / 2, numberOfSellers / 2, 
          3 * numberOfSellers / 4)
        tradingRun.run()
        tradingRun = new DoTrades(0, maxNumberOfTrades / 4, numberOfBuyers / 2, 3 * numberOfBuyers / 4, 
          numberOfSellers / 4, numberOfSellers / 2)
        tradingRun.run()
        tradingRun = new DoTrades(0, maxNumberOfTrades / 4, 3 * numberOfBuyers / 4, numberOfBuyers, 1, 
          numberOfSellers / 4)
        tradingRun.run()

    }
  }

  def DoMultiThreadedTrades() {
    val t = Array.ofDim[Thread](numberOfThreads)
    var done = false
    val tradesPerThread = maxNumberOfTrades / numberOfThreads
    val buyersPerThread = numberOfBuyers / numberOfThreads
    val sellersPerThread = numberOfSellers / numberOfThreads
    for (i <- 0 until numberOfThreads) {
      t(i) = if (!doNew) new Thread(new DoTrades(i, tradesPerThread, i * buyersPerThread + 1, (i + 1) * buyersPerThread, 
        i * sellersPerThread + 1, (i + 1) * sellersPerThread)) else new Thread(new DoTradesNew(i, tradesPerThread, 
        i * buyersPerThread + 1, (i + 1) * buyersPerThread, i * sellersPerThread + 1, (i + 1) * sellersPerThread))
      t(i).start()
      //println("Spinning up thread")
    }
    while (!done) {
      done = true
      for (i <- 0 until numberOfThreads if t(i).isAlive) {
        done = false
      }
      try {
        Thread.sleep(sleepDuration)
      } catch {
        case ie: InterruptedException => 
      }
    }
  }

  class DoTradesNew(var threadID: Int, 
      var maxTrades: Int, 
      startBuyerIndex: Int, 
      endBuyerIndex: Int, 
      startSellerIndex: Int, 
      endSellerIndex: Int) extends Runnable {

    var beginBuyers: Int = 1

    var endBuyers: Int = _

    var beginSellers: Int = 1

    var endSellers: Int = _

    //var randomStream: Random = new Random()

    def run() {
      var buyerIndex: Int = 0
      var sellerIndex: Int = 0
      var bidPrice: Double = 0.0
      var askPrice: Double = 0.0
      var transactionPrice: Double = 0.0
      var counter = 1
      while (counter <= maxTrades) {
        do {
          //buyerIndex = beginBuyers + randomStream.nextInt(endBuyers - beginBuyers + 1) - 
          buyerIndex = beginBuyers + ThreadLocalRandom.current().nextInt(endBuyers - beginBuyers + 1) - 
            1
          if (!people.buyers(buyerIndex).lock.tryAcquire()) {
            //continue
          }
          if (people.buyers(buyerIndex).hasTraded()) {
            people.buyers(buyerIndex).lock.release()
            //continue
          } else {
            //break
          }
        } while (true);
        bidPrice = people.buyers(buyerIndex).formBidPrice()
        do {
          sellerIndex = beginSellers + 
            //randomStream.nextInt(endSellers - beginSellers + 1) - 
            ThreadLocalRandom.current().nextInt(endSellers - beginSellers + 1) -
            1
          if (!people.sellers(sellerIndex).lock.tryAcquire()) {
            //continue
          }
          if (people.sellers(sellerIndex).hasTraded()) {
            people.sellers(sellerIndex).lock.release()
            //continue
          } else {
            //break
          }
        } while (true);
        askPrice = people.sellers(sellerIndex).formAskPrice()
        if (bidPrice > askPrice) {
          //transactionPrice = askPrice + randomStream.nextDouble() * (bidPrice - askPrice)
          transactionPrice = askPrice + ThreadLocalRandom.current().nextDouble(0, 1) * (bidPrice - askPrice)
          people.buyers(buyerIndex).setTraded(true)
          people.buyers(buyerIndex).price = transactionPrice
          people.sellers(sellerIndex).setTraded(true)
          people.sellers(sellerIndex).price = transactionPrice
          Transactions(threadID).AddDatuum(transactionPrice)
          if (verboseOutput) {
            System.out.print(threadID + "Found two agents willing to trade: ")
            println("Price of " + twoDigits.format(transactionPrice) + " with buyer's bid of " + 
              twoDigits.format(bidPrice) + 
              " and seller asking price of " + 
              twoDigits.format(askPrice))
          }
        } else if (verboseOutput) {
          println(threadID + "Could not trade: " + "; buyer's bid was " + 
            twoDigits.format(bidPrice) + 
            " while seller's asking price was " + 
            twoDigits.format(askPrice))
        }
        people.buyers(buyerIndex).lock.release()
        people.sellers(sellerIndex).lock.release()
        counter += 1
      }
    }
  }

  class DoTrades(var threadID: Int, 
      var maxTrades: Int, 
      var beginBuyers: Int, 
      var endBuyers: Int, 
      var beginSellers: Int, 
      var endSellers: Int) extends Runnable {

    //var randomStream: Random = new Random()

    def run() {
      var buyerIndex: Int = 0
      var sellerIndex: Int = 0
      var bidPrice: Double = 0.0
      var askPrice: Double = 0.0
      var transactionPrice: Double = 0.0
      var counter = 1
      while (counter <= maxTrades) {
        do {
          //buyerIndex = beginBuyers + randomStream.nextInt(endBuyers - beginBuyers + 1) - 
          buyerIndex = beginBuyers + ThreadLocalRandom.current().nextInt(endBuyers - beginBuyers + 1) - 
            1
        } while (people.buyers(buyerIndex).hasTraded());
        bidPrice = people.buyers(buyerIndex).formBidPrice()
        do {
          sellerIndex = beginSellers + 
            //randomStream.nextInt(endSellers - beginSellers + 1) - 
            ThreadLocalRandom.current().nextInt(endSellers - beginSellers + 1) - 
            1
        } while (people.sellers(sellerIndex).hasTraded());
        askPrice = people.sellers(sellerIndex).formAskPrice()
        if (bidPrice > askPrice) {
          //transactionPrice = askPrice + randomStream.nextDouble() * (bidPrice - askPrice)
          transactionPrice = askPrice + ThreadLocalRandom.current().nextDouble() * (bidPrice - askPrice)
          people.buyers(buyerIndex).setTraded(true)
          people.buyers(buyerIndex).price = transactionPrice
          people.sellers(sellerIndex).setTraded(true)
          people.sellers(sellerIndex).price = transactionPrice
          Transactions(threadID).AddDatuum(transactionPrice)
          if (verboseOutput) {
            System.out.print(threadID + "Found two agents willing to trade: ")
            println("Price of " + twoDigits.format(transactionPrice) + " with buyer's bid of " + 
              twoDigits.format(bidPrice) + 
              " and seller asking price of " + 
              twoDigits.format(askPrice))
          }
        } else if (verboseOutput) {
          println(threadID + "Could not trade: " + "; buyer's bid was " + 
            twoDigits.format(bidPrice) + 
            " while seller's asking price was " + 
            twoDigits.format(askPrice))
        }
        counter += 1
      }
    }
  }

  private def OpenTrading() {
    println("\nZero-Intelligence Traders")
    val startTime = Calendar.getInstance.getTimeInMillis
    if (!threadedTrades) {
      DoSingleThreadedTrades()
    } else {
      DoMultiThreadedTrades()
    }
    val finishTime = Calendar.getInstance.getTimeInMillis
    for (i <- 1 until numberOfThreads) {
      Transactions(0).CombineWithDifferentData(Transactions(i))
    }
    println("\nFinal stats: " + Transactions(0).GetN() + " transactions at " + 
      twoDigits.format(Transactions(0).GetAverage()) + 
      " average price; " + 
      twoDigits.format(Transactions(0).GetStdDev()) + 
      " standard deviation.\n")
    println("Elapsed time: " + (finishTime - startTime) + " milliseconds\n")
  }
}

