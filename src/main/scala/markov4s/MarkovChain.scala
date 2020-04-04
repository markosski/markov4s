package markov4s

import scala.collection.immutable.HashMap
import scala.annotation.tailrec
import MarkovChain.Data

object MarkovChain {
  type Data[A] = HashMap[A, HashMap[A, Int]]

  /**
    * Primary access point to construct empty MarkovChain.
    */
  def apply[A]: MarkovChain[A] =
    MarkovChain(
      HashMap[A, HashMap[A, Int]]()
    )

  def sequenceRandom[A](iterations: Int, vecSize: Int, f: Int => Vector[A]): List[Vector[A]] = {
    val result = (0 to iterations).scanLeft(Vector[A]()) { (a, b) => 
      f(vecSize)
    }
    result.tail.toList
  }

  def sequence[A](iterations: Int, vecSize: Int, elem: A, f: (A, Int) => Vector[A]): List[Vector[A]] = {
    val result = (0 to iterations).scanLeft(Vector[A]()) { (a, b) => 
      f(elem, vecSize)
    }
    result.tail.toList
  }
}

final case class MarkovChain[A](data: Data[A]) {
  /**
    * Add single element to the chain
    */
  def put(a: A, b: A): MarkovChain[A] = {
    val newData: Data[A] = data.get(a) match {
      case Some(elemA) => {
        elemA.get(b) match {
          case Some(elemB)  => data + (a -> (elemA + (b -> (elemB + 1))))
          case None		      => data + (a -> (elemA + (b -> 1)))
        }
      }
      case None => data + (a -> HashMap(b -> 1))
    }

    MarkovChain(newData)
  }

  /**
    * Combine two instances of MarkovChain
    */
  def join(that: MarkovChain[A]): MarkovChain[A] = {
    val (lhs, rhs) = if (data.size > that.data.size) (data, that.data) else (that.data, data)

    val newData = rhs.keys.toList.foldLeft(lhs) { (ch, k) =>
      ch.get(k) match {
        case Some(chain) => ch + (k -> chain.merged(rhs(k)) { (a, b) => (a._1, a._2 + b._2) } )
        case None => ch + (k -> rhs(k))
      }
    }

    MarkovChain(newData)
  }

  /**
    * Alias for `put`
    */
  def +(pair: (A, A)): MarkovChain[A] = put(pair._1, pair._2)

  /**
    * Alias for `join`
    */
  def ++(markovChain: MarkovChain[A]): MarkovChain[A] = join(markovChain)

  /**
    * Create chain from sequence of items. Relationship will be created by making connections between each consecutive item.
    */
  def fromSeq(xs: Seq[A]): MarkovChain[A] = {
    xs.sliding(2).foldLeft(this)((a, b) => a.put(b.head, b.tail.head))
  }

  /**
    * Get single item from probability distribution.
    */
  def getWithProb(a: A)(implicit rand: RandLike): Option[A] = {
    val nextDouble: Double = rand.nextDouble
    data.get(a) match {
      case Some(elemA) => {
        val probs = calculateChainProb(a, elemA)
        val els = probs.toList.sortBy(_._2)

        var result = a
        var prev = 0.0
        for (el <- els) {
          if (nextDouble >= prev && nextDouble <= prev + el._2) result = el._1
          prev = prev + el._2
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
        val probs = calculateChainProb(a, elemA)
        probs.toList.sortBy(_._2).reverse.take(num).map(x => x._1)
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
  def getRandomSeqWithProb(atMost: Int)(implicit rand: RandLike): Vector[A] = {
    val elem = getRandomElement(rand)
    getSeqWithProb(elem, atMost)(rand)
  }

  /**
    * Get sequence of elements selecting highest probability elements starting with random element.
    */
  def getRandomSeq(atMost: Int)(implicit rand: RandLike): Vector[A] = {
    val elem = getRandomElement(rand)
    getSeq(elem, atMost)
  }

  private def getRandomElement(rand: RandLike): A = {
    val index = math.abs(rand.nextInt) % data.keys.size

    data.keys.toList(index)
  }

  /**
    * Get sequence of elements using probability distribution.
    */
  def getSeqWithProb(a: A, atMost: Int)(implicit rand: RandLike): Vector[A] = {
    if (atMost > 0)
      doInnerSeq(a, Vector[A](a), math.max(0, atMost-1))(getWithProb)
    else
      Vector[A]()
  }

  /**
    * Get sequence of elements using highest probability value.
    */
  def getSeq(a: A, atMost: Int): Vector[A] = getSeqWith(a, atMost)(get)

  private def calculateChainKeyCount(chain: HashMap[A, Int]): Int = {
    chain.values.sum
  }

  private def calculateChainProb(key: A, chain: HashMap[A, Int]): HashMap[A, Double] = {
    chain.keys.toList.foldLeft(HashMap[A, Double]()) { (ch, k) => 
      ch + (k -> chain(k) / calculateChainKeyCount(chain).toDouble)
    }
  }

  /**
   * Produce function with no-op RNG. Used to allow composition in doInnverSeq.
   */
  private def liftNoOp(f: A => Option[A]) = {
    (a: A) => f(a)
  }

  private def getSeqWith(a: A, atMost: Int)(f: A => Option[A]): Vector[A] = {
    if (atMost > 0)
      doInnerSeq(a, Vector[A](a), math.max(0, atMost-1))(liftNoOp(f))
    else
      Vector[A]()
  }

  @tailrec
  private def doInnerSeq(a: A, as: Vector[A], num: Int)(f: A => Option[A]): Vector[A] = {
    num match {
      case 0 => as
      case _ => {
        f(a) match {
          case Some(a1) => doInnerSeq(a1, as :+ a1, num-1)(f)
          case None => as
        }
      }
    }
  }
}