package ZIMarketParallel

import java.util.concurrent._

class BuyerAgent(maxBuyerValue: Double) {


  //private var cost: Double = ThreadLocalRandom.current().nextDouble(0, 1) * maxCost
  private var value: Double = ThreadLocalRandom.current().nextDouble(0, 1) * maxBuyerValue

  private var traded: Boolean = false

  var price: Double = _

  var lock: Semaphore = new Semaphore(1)

  def setTraded(t: Boolean) {
    traded = t
  }

  def hasTraded(): Boolean = (traded)

  //def formBidPrice(): Double = Math.random() * value
  def formBidPrice(): Double = ThreadLocalRandom.current().nextDouble(0, 1) * value
}