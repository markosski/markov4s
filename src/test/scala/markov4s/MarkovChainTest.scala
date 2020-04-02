package markov4s

import collection.mutable.Stack
import org.scalatest._
import scala.collection.immutable.HashMap

class MarkovChainTest extends FlatSpec with Matchers {
  "Two chains with the same data" should "be equal." in {
    val chain1 = MarkovChain[String] + ("a" -> "b") + ("b" -> "c")
    val chain2 = MarkovChain[String] + ("b" -> "c") + ("a" -> "b")

    chain1 shouldEqual chain2
  }

  ".getVec" should "return a vector of top matching values." in {
    val chain = MarkovChain[String] + ("a" -> "b") + ("b" -> "c")
    chain.getSeq("a", 3) shouldEqual Vector[String]("a", "b", "c")
    chain.getSeq("a", 2) shouldEqual Vector("a", "b")
    chain.getSeq("a", 1) shouldEqual Vector("a")
    chain.getSeq("a", 0) shouldEqual Vector()
    chain.getSeq("a", -1) shouldEqual Vector()
  }

  ".getRandomVec" should "return a vector of top matching value with random initial element." in {
    val chain = MarkovChain[String] + 
    ("a" -> "b") +
    ("b" -> "c") + 
    ("c" -> "a")

    // Compare getRandomVec in terms of getVec
    val result = chain.getRandomSeq(new Rand(1))(3)
    val expected = chain.getSeq(result.head, 3)

    result shouldEqual expected
  }

  ".getRandomVecWithProb" should "return a vector of top matching value with random initial element." in {
    val chain = MarkovChain[String] + 
    ("a" -> "b") +
    ("b" -> "c") + 
    ("c" -> "a") +
    ("a" -> "b") +
    ("c" -> "d")

    // Compare getRandomVec in terms of getVecWithProb
    val rand1 = new Rand(1)
    val rand2 = new Rand(1)
    val result = chain.getRandomSeqWithProb(rand1)(3)
    val expected = chain.getSeqWithProb(rand2)(result.head, 3)

    result shouldEqual expected
  }

  ".getTop" should "return N top elements." in {
    val chain = MarkovChain[String].fromSeq(List("a", "b", "c"))
    chain.getTop("a", 1) shouldEqual List("b")
    chain.getTop("b", 1) shouldEqual List("c")
    chain.getTop("c", 1) shouldEqual List()

    val chain2 = MarkovChain[String].fromSeq(List("a", "b", "c", "a", "c"))
    chain2.getTop("a", 1) shouldEqual List("c")
    chain2.getTop("a", 2) shouldEqual List("c", "b")
    chain2.getTop("a", 3) shouldEqual List("c", "b")
  } 
  
  it should "return empty sequence for <= 0 argument." in {
    val chain = MarkovChain[String].fromSeq(List("a", "b", "c"))
    chain.getTop("a", 0) shouldEqual List()
    chain.getTop("a", -1) shouldEqual List()
  }

  ".get, .getTop and .getWitProb" should "return self." in {
    val chain = MarkovChain[String] + ("a" -> "a") + ("b" -> "b")
    chain.get("a") shouldEqual Some("a")
    chain.get("b") shouldEqual Some("b")

    chain.getTop("a", 1) shouldEqual List("a")
    chain.getTop("b", 1) shouldEqual List("b")

    val rand = new Rand(1)
    chain.getWithProb(rand)("a") shouldEqual Some("a")
    chain.getWithProb(rand)("b") shouldEqual Some("b")
  }

  ".fromSeq and .put operators" should "yield the same result." in {
    val chain1 = MarkovChain[String].fromSeq(List("a", "b", "c"))
    val chain2 = MarkovChain[String] + ("a" -> "b") + ("b" -> "c")

    chain1.getSeq("a", 3) shouldEqual chain2.getSeq("a", 3)
  }

  "fromSeq" should "construct chain" in {
    val chain = MarkovChain[String]
      .fromSeq(List("a", "b", "c"))
      .fromSeq(List("a", "b", "d", "e"))

    (chain).data shouldEqual (HashMap(
      "a" -> HashMap("b" -> 2),
      "b" -> HashMap("c" -> 1, "d" -> 1),
      "d" -> HashMap("e" -> 1)
    ))
  }

  "++ / join simple" should "join two chains" in {
    val chain1 = MarkovChain[String].fromSeq(List("a", "b", "c"))
    val chain2 = MarkovChain[String].fromSeq(List("x", "y", "z"))
    val expected = (HashMap(
      "a" -> HashMap("b" -> 1),
      "b" -> HashMap("c" -> 1),
      "x" -> HashMap("y" -> 1),
      "y" -> HashMap("z" -> 1)
    ))

    (chain1 ++ chain2).data shouldEqual expected
    (chain1 join chain2).data shouldEqual expected
  }

  "++ / join complex" should "join two chains" in {
    val chain1 = MarkovChain[String].fromSeq(List("a", "b", "c"))
    val chain2 = MarkovChain[String].fromSeq(List("a", "b", "d", "e"))
    val expected = HashMap(
      "a" -> HashMap("b" -> 2),
      "b" -> HashMap("c" -> 1, "d" -> 1),
      "d" -> HashMap("e" -> 1)
    )

    (chain1 ++ chain2).data shouldEqual expected
    (chain1 join chain2).data shouldEqual expected
  }
}