package markov4s

import scala.collection.immutable.HashMap
import scala.annotation.tailrec
import MarkovChain.{Counter, Data}

final case class MarkovNode(count: Int, prob: Double)

object MarkovChain {
  type Data[A] = HashMap[A, HashMap[A, MarkovNode]]
  type Counter[A] = HashMap[A, Int]

  /**
    * Primary access point to construct empty MarkovChain.
    */
  def apply[A]: MarkovChain[A] = {
    MarkovChain(
      HashMap[A, HashMap[A, MarkovNode]](),
      HashMap[A, Int]()
    )
  }

  def sequenceRandomVec[A](rng: RNG, iterations: Int, vecSize: Int, f: RNG => Int => (RNG, Vector[A])): (RNG, List[Vector[A]]) = {
    val result = (0 to iterations).scanLeft((rng, Vector[A]())) { (a, b) => 
      f(a._1)(vecSize)
    }
    (result.last._1, result.map(_._2).tail.toList)
  }

  def sequenceVec[A](rng: RNG, iterations: Int, vecSize: Int, elem: A, f: RNG => (A, Int) => (RNG, Vector[A])): (RNG, List[Vector[A]]) = {
    val result = (0 to iterations).scanLeft((rng, Vector[A]())) { (a, b) => 
      f(a._1)(elem, vecSize)
    }
    (result.last._1, result.map(_._2).tail.toList)
  }
}

final case class MarkovChain[A](data: Data[A], counter: Counter[A]) {
  /**
    * Add single element.
    */
  def put(a: A, b: A): MarkovChain[A] = {
    val newData: Data[A] = data.get(a) match {
      case Some(elemA) => {
        elemA.get(b) match {
          case Some(elemB)  => data + (a -> (elemA + (b -> MarkovNode(elemB.count + 1, 0.0))))
          case None		      => data + (a -> (elemA + (b -> MarkovNode(1, 0.0))))
        }
      }
      case None => data + (a -> HashMap(b -> MarkovNode(1, 0.0)))
    }
    val newCounter = counter + (a -> (counter.getOrElse(a, 0) + 1))

    MarkovChain(updateNodes(a, newData, newCounter), newCounter)
  }

  /**
    * Alias for `put`.
    */
  def +(pair: (A, A)): MarkovChain[A] = put(pair._1, pair._2)

  def fromSeq(xs: Seq[A]): MarkovChain[A] = {
    xs.sliding(2).foldLeft(this)((a, b) => a.put(b.head, b.tail.head))
  }

  /**
    * Update all elements and their probability values.
    */
  private def updateNodes(key: A, data: Data[A], counters: Counter[A]): Data[A] = {
    data.get(key) match {
      case Some(el) => {
        val newElem = el.keys.toList.foldLeft(el) { (e, x) =>
          e + (x -> MarkovNode(e(x).count, e(x).count / counters(key).toDouble))
        }
        data + (key -> newElem)
      }
      case None => data
    }
  }

  /**
    * Get single item from probability distribution.
    */
  def getWithProb(rng: RNG)(a: A): (RNG, Option[A]) = {
    val nextRng = rng.nextInt
    val rngDouble: Double = RNG.toDouble(nextRng._1)
    data.get(a) match {
      case Some(elemA) => {
        val els = elemA.toList.sortBy(_._2.prob)

        var result = a
        var prev = 0.0
        for (el <- els) {
          if (rngDouble >= prev && rngDouble <= prev + el._2.prob) result = el._1
          prev = prev + el._2.prob
        }

        (nextRng._2, Some(result))
      }
      case None => (rng, None)
    }
  }

  /**
    * Get top N items with highest probability value.
    */
  def getTop(a: A, num: Int): List[A] = {
    data.get(a) match {
      case Some(elemA) => {
        elemA.toList.sortBy(_._2.prob).reverse.take(num).map(x => x._1)
      }
      case None => List[A]()
    }
  }

  /**
    * Get single item with highest probability value.
    */
  def get(a: A): Option[A] = getTop(a, 1).headOption

  /**
    * Get sequence of elements using probability distribution starting with random element.
    */
  def getRandomVecWithProb(rng: RNG)(atMost: Int): (RNG, Vector[A]) = {
    val elem = getRandomElement(rng)
    (elem._1, getVecWithProb(rng)(elem._2, atMost)._2)
  }

  /**
    * Get sequence of elements selecting highest probability elements starting with random element.
    */
  def getRandomVec(rng: RNG)(atMost: Int): (RNG, Vector[A]) = {
    val elem = getRandomElement(rng)
    (elem._1, getVec(elem._2, atMost))
  }

  private def getRandomElement(rng: RNG): (RNG, A) = {
    val nextRng = rng.nextInt
    val index = math.abs(nextRng._1) % data.keys.size

    (nextRng._2, data.keys.toList(index))
  }

  /**
    * Get sequence of elements using probability distribution.
    */
  def getVecWithProb(rng: RNG)(a: A, atMost: Int): (RNG, Vector[A]) = {
    if (atMost > 0)
      doInnerVec(a, Vector[A](a), math.max(0, atMost-1), rng)(getWithProb)
    else
      (rng, Vector[A]())
  }

  /**
    * Get sequence of elements using highest probability value.
    */
  def getVec(a: A, atMost: Int): Vector[A] = getVecWith(a, atMost)(get)

  /**
   * Produce function with no-op RNG. Used to allow composition in doInnverVec.
   */
  private def liftNoOpRNG(f: A => Option[A]) = {
    (rng: RNG) => (a: A) => (rng, f(a))
  }

  private def getVecWith(a: A, atMost: Int)(f: A => Option[A]): Vector[A] = {
    if (atMost > 0)
      doInnerVec(a, Vector[A](a), math.max(0, atMost-1), RNG(0))(liftNoOpRNG(f))._2
    else
      Vector[A]()
  }

  @tailrec
  private def doInnerVec(a: A, as: Vector[A], num: Int, rng: RNG)(f: RNG => A => (RNG, Option[A])): (RNG, Vector[A]) = {
    num match {
      case 0 => (rng, as)
      case _ => {
        f(rng)(a) match {
          case (newRNG: RNG, Some(a1)) => doInnerVec(a1, as :+ a1, num-1, newRNG)(f)
          case (newRNG: RNG, None) => (newRNG, as)
        }
      }
    }
  }
}