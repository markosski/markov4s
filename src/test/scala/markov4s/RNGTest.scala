package markov4s

import org.scalactic.TolerantNumerics
import collection.mutable.Stack
import org.scalatest._

class RNGTest extends FlatSpec with Matchers {
    "RNG toDouble int conversion" should "average 0.5." in {
        val eps = 0.01

        val num = (0 to 100000).scanLeft(RNG.init.nextInt)((a, b) => a._2.nextInt).map(x => RNG.toDouble(x._1)) 
        
        val result = num.sum/num.size 
        result should be (0.5d +- eps)
    }
}
