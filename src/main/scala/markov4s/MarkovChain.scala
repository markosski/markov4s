package markov4s

import scala.collection.immutable.HashMap
import scala.math.random
import scala.util.Random
import MarkovChain.{Counter, Data}
import scala.annotation.tailrec

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
  def getWithProb(a: A): Option[A] = {
    data.get(a) match {
      case Some(elemA) => {
        val rand = random
        val els = elemA.toList.sortBy(_._2.prob)

        var result = a
        var prev = 0.0
        for (el <- els) {
          if (rand >= prev && rand <= prev + el._2.prob) result = el._1
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
   * Pick random element and return a pair of random element and next element.
   */
  def getRandomTop: (A, A) = getRandomInner(get)

  /**
   * Pick random element and return a pair of random element and next element.
   */
  def getRandomWithProb: (A, A) = getRandomInner(getWithProb)

  /**
    * Get sequence of elements using probability distribution starting with random element.
    */
  def getRandomVecWithProb(atMost: Int): Vector[A] = {
    val elem = getRandomElement()
    getVecWithProb(elem, atMost)
  }

  /**
    * Get sequence of elements selecting highest likelyhood elements starting with random element.
    */
  def getRandomVec(atMost: Int): Vector[A] = {
    val elem = getRandomElement()
    getVec(elem, atMost)
  }

  private def getRandomElement(): A = {
    val rand = Random.nextInt(data.keys.size)
    data.keys.toList(rand)
  }

  private def getRandomInner(f: A => Option[A]): (A, A) = {
    val elem = getRandomElement()
    (elem -> f(elem).get)
  }

  /**
    * Get sequence of elements using probability distribution.
    */
  def getVecWithProb(a: A, atMost: Int): Vector[A] = getVecWith(a, atMost)(getWithProb)

  /**
    * Get sequence of elements using highest probability value.
    */
  def getVec(a: A, atMost: Int): Vector[A] = getVecWith(a, atMost)(get)

  private def getVecWith(a: A, atMost: Int)(f: A => Option[A]) = {
    if (atMost > 0)
      doInnerVec(a, Vector[A](a), math.max(0, atMost-1))(f)
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