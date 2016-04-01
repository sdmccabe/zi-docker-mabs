package ZIMarketParallel

import java.util.concurrent._

class SellerAgent(private var maxCost: Double) {

  //private var cost: Double = Math.random() * maxCost
  private var cost: Double = ThreadLocalRandom.current().nextDouble(0, 1) * maxCost
  
  var traded: Boolean = false

  var price: Double = _

  var lock: Semaphore = new Semaphore(1)

  def setTraded(t: Boolean) {
    traded = t
  }

  def hasTraded(): Boolean = (traded)

  //def formAskPrice(): Double = cost + Math.random() * (maxCost - cost)  
	def formAskPrice(): Double = cost + ThreadLocalRandom.current().nextDouble(0, 1) * (maxCost - cost)
}

