package ZIMarketParallel

class AgentPopulation(var nBuyers: Int, 
    maxValue: Double, 
    var nSellers: Int, 
    maxCost: Double) {

  var buyers: Array[BuyerAgent] = new Array[BuyerAgent](nBuyers)

  var sellers: Array[SellerAgent] = new Array[SellerAgent](nSellers)

  for (i <- 0 until buyers.length) {
    buyers(i) = new BuyerAgent(maxValue)
  }

  for (i <- 0 until sellers.length) {
    sellers(i) = new SellerAgent(maxCost)
  }

  def getActiveBuyers(): Int = {
    var count = 0
    for (i <- 0 until nBuyers if buyers(i).hasTraded()) {
      count += 1
    }
    (count)
  }

  def getActiveSellers(): Int = {
    var count = 0
    for (i <- 0 until nSellers if sellers(i).hasTraded()) {
      count += 1
    }
    (count)
  }
}