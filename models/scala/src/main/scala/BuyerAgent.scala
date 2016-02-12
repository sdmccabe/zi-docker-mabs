package ZIMarketParallel

import java.util.concurrent._

class BuyerAgent(maxBuyerValue: Double) {

  private var value: Double = Math.random() * maxBuyerValue

  private var traded: Boolean = false

  var price: Double = _

  var lock: Semaphore = new Semaphore(1)

  def setTraded(t: Boolean) {
    traded = t
  }

  def hasTraded(): Boolean = (traded)

  def formBidPrice(): Double = Math.random() * value
}