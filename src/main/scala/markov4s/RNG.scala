package markov4s

/**
 * Random number generator taken from the "red book" - Functional Programmin in Scala.
 */
trait RNG {
  def nextInt: (Int, RNG)
}
  
object RNG {
  def init = apply(System.currentTimeMillis)

  def apply(seed: Long): RNG = new RNG {
    val seed2 = (seed*0x5DEECE66DL + 0xBL) & ((1L << 48) - 1)
    def nextInt = {
        ((seed2 >>> 16).asInstanceOf[Int], apply(seed2))
    }
    override def toString = s"RNG@$seed"
  }

  def toDouble(seed: Int): Double = {
    math.abs(seed) / (Int.MaxValue.toDouble + 1)
  }
}