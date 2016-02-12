package ZIMarketParallel

class Data {

  private var N: Int = 0

  private var min: Double = 1000000000.0

  private var max: Double = 0.0

  private var sum: Double = 0.0

  private var sum2: Double = 0.0

  def AddDatuum(Datuum: Double) : Unit = {
    N += 1
    if (Datuum < min) {
      min = Datuum
    }
    if (Datuum > max) {
      max = Datuum
    }
    sum += Datuum
    sum2 += Datuum * Datuum
  }

  def GetN(): Int = N

  def GetMin(): Double = min

  def GetMax(): Double = max

  def GetAverage(): Double = if (N > 0) {
  sum / N
} else {
  0.0
}

  def GetVariance(): Double = {
    var avg: Double = 0.0
    var arg: Double = 0.0
    if (N > 1) {
      avg = GetAverage()
      arg = sum2 - N * avg * avg
      arg / (N - 1)
    } else {
      0.0
    }
  }

  def GetStdDev(): Double = Math.sqrt(GetVariance())

  def CombineWithDifferentData(otherData: Data) {
    N += otherData.GetN()
    min = Math.min(min, otherData.GetMin())
    max = Math.max(max, otherData.GetMax())
    sum += otherData.GetN() * otherData.GetAverage()
    sum2 += otherData.GetVariance() * (otherData.GetN() - 1) + 
      otherData.GetN() * otherData.GetAverage() * otherData.GetAverage()
  }
}