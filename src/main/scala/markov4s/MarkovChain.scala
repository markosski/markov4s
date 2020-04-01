package markov4s

import scala.collection.mutable.HashMap
import scala.annotation.tailrec
import MarkovChain.{Counter, Data}

final case class MarkovNode(count: Int, prob: Double)

object MarkovChain {
  type Data[A] = HashMap[A, HashMap[A, MarkovNode]]
  type Counter[A] = HashMap[A, Int]

  /**
    * Primary access point to construct empty MarkovChain.
    */
  def apply[A]: MarkovChain[A] =
    MarkovChain(
      HashMap[A, HashMap[A, MarkovNode]](),
      HashMap[A, Int](),
      Rand()
    )

  def apply[A](rand: RandLike): MarkovChain[A] =
    MarkovChain(
      HashMap[A, HashMap[A, MarkovNode]](),
      HashMap[A, Int](),
      rand
    )

  def sequenceRandomVec[A](s: RNG, iterations: Int, vecSize: Int, f: Int => Vector[A]): List[Vector[A]] = {
    val result = (0 to iterations).scanLeft(Vector[A]()) { (a, b) => 
      f(vecSize)
    }
    result.tail.toList
  }

  def sequenceVec[A](rng: RNG, iterations: Int, vecSize: Int, elem: A, f: RNG => (A, Int) => (RNG, Vector[A])): (RNG, List[Vector[A]]) = {
    val result = (0 to iterations).scanLeft((rng, Vector[A]())) { (a, b) => 
      f(a._1)(elem, vecSize)
    }
    (result.last._1, result.map(_._2).tail.toList)
  }
}

final case class MarkovChain[A](data: Data[A], counter: Counter[A], rand: RandLike) {
  def equals(that: MarkovChain[A]): Boolean = {
    if (that.data == data && that.counter == counter) true
    else false
  }
  /**
    * Add single element.
    */
  def put(a: A, b: A): MarkovChain[A] = {
    data.get(a) match {
      case Some(elemA) => {
        elemA.get(b) match {
          case Some(elemB)  => {
            elemA.update(b, MarkovNode(elemB.count + 1, 0.0))
            data.update(a, elemA)
          }
          case None		      => {
            elemA.update(b, MarkovNode(1, 0.0))
            data.update(a, elemA)
          }
        }
      }
      case None => {
        data.update(a, HashMap(b -> MarkovNode(1, 0.0)))
      }
    }

    counter.update(a, (counter.getOrElse(a, 0) + 1))

    MarkovChain(updateNodes(a, data, counter), counter, rand)
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
          e.update(x, MarkovNode(e(x).count, e(x).count / counters(key).toDouble))
          e
        }
        data.update(key, newElem)
        data
      }
      case None => data
    }
  }

  /**
    * Get single item from probability distribution.
    */
  def getWithProb(a: A): Option[A] = {
    val nextInt = rand.nextInt
    val newRand = rand.make(nextInt)
    val nextDouble: Double = newRand.nextDouble
    data.get(a) match {
      case Some(elemA) => {
        val els = elemA.toList.sortBy(_._2.prob)

        var result = a
        var prev = 0.0
        for (el <- els) {
          if (nextDouble >= prev && nextDouble <= prev + el._2.prob) result = el._1
          prev = prev + el._2.prob
        }

        Some(result)
      }
      case None => None
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
  def getRandomVecWithProb(atMost: Int): Vector[A] = {
    val elem = getRandomElement
    getVecWithProb(elem, atMost)
  }

  /**
    * Get sequence of elements selecting highest probability elements starting with random element.
    */
  def getRandomVec(atMost: Int): Vector[A] = {
    val elem = getRandomElement
    getVec(elem, atMost)
  }

  private def getRandomElement: A = {
    val index = math.abs(rand.nextInt) % data.keys.size

    data.keys.toList(index)
  }

  /**
    * Get sequence of elements using probability distribution.
    */
  def getVecWithProb(a: A, atMost: Int): Vector[A] = {
    if (atMost > 0)
      doInnerVec(a, Vector[A](a), math.max(0, atMost-1))(getWithProb)
    else
      Vector[A]()
  }

  /**
    * Get sequence of elements using highest probability value.
    */
  def getVec(a: A, atMost: Int): Vector[A] = getVecWith(a, atMost)(get)

  /**
   * Produce function with no-op RNG. Used to allow composition in doInnverVec.
   */
  private def liftNoOpRNG(f: A => Option[A]) = {
    (a: A) => f(a)
  }

  private def getVecWith(a: A, atMost: Int)(f: A => Option[A]): Vector[A] = {
    if (atMost > 0)
      doInnerVec(a, Vector[A](a), math.max(0, atMost-1))(liftNoOpRNG(f))
    else
      Vector[A]()
  }

  @tailrec
  private def doInnerVec(a: A, as: Vector[A], num: Int)(f: A => Option[A]): Vector[A] = {
    num match {
      case 0 => as
      case _ => {
        f(a) match {
          case Some(a1) => doInnerVec(a1, as :+ a1, num-1)(f)
          case None => as
        }
      }
    }
  }
}