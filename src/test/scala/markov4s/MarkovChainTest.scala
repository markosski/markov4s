package markov4s

import collection.mutable.Stack
import org.scalatest._

class MarkovChainTest extends FlatSpec with Matchers {
  ".getVec" should "return a vector of top matching values." in {
    val chain = MarkovChain[String] + ("a" -> "b") + ("b" -> "c")
    chain.getVec("a", 3) shouldEqual Vector[String]("a", "b", "c")
    chain.getVec("a", 2) shouldEqual Vector("a", "b")
    chain.getVec("a", 1) shouldEqual Vector("a")
    chain.getVec("a", 0) shouldEqual Vector()
    chain.getVec("a", -1) shouldEqual Vector()
  }

  ".getRandomVec" should "return a vector of top matching value with random initial element." in {
    val chain = MarkovChain[String] + 
    ("a" -> "b") +
    ("b" -> "c") + 
    ("c" -> "a")

    // Compare getRandomVec in terms of getVec
    val result = chain.getRandomVec(3)
    val expected = chain.getVec(result.head, 3)

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

  ".getTop" should "return empty sequence for <= 0 argument." in {
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

    chain.getWithProb("a") shouldEqual Some("a")
    chain.getWithProb("b") shouldEqual Some("b")
  }

  ".fromSeq and .put operators" should "yield the same result." in {
    val chain1 = MarkovChain[String].fromSeq(List("a", "b", "c"))
    val chain2 = MarkovChain[String] + ("a" -> "b") + ("b" -> "c")

    chain1.getVec("a", 3) shouldEqual chain2.getVec("a", 3)
  }
}