package markov4s

import scala.util.Random

/**
 * Random number generator taken from the "red book" - Functional Programmin in Scala.
 */
trait RNG {
  def nextInt: (Int, RNG)
}

trait RandLike {
  def nextInt: Int
  def nextDouble: Double
  def make(seed: Int): RandLike
}

class Rand(seed: Int) extends RandLike {
  val r = new Random(seed)
  def nextInt = r.nextInt()
  def nextDouble = r.nextDouble()
  def make(seed: Int) = new Rand(seed)
}

object Rand {
  def apply() = new Rand(Random.nextInt)
}