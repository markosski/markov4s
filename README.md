
# Markov4s [![Build Status](https://travis-ci.org/markosski/cloudsync.svg?branch=master)](https://travis-ci.org/markosski/markov4s)
Markov4s is a small library that introduces immutable data structure to represent Markov Chain.

Package location:
https://bintray.com/markosski/maven/markov4s

Usage:

```
import markov4s._

// empty chain
val chain = MarkovChain[Int]

// add relationship between node of value 0 and 1
val chain1 = chain + (0 -> 1) + (1 -> 2) + (2 -> 3)
val chain2 = chain1.getVec(0, 4)

// result: Vector(0, 1, 2, 3)
```

## Creating chain from sequence.

```
val chain = MarkovChain[String].
    fromSeq(List("hello", "world", "how", "are", "you", "!")).
    fromSeq(List("hello", "marcin", "how", "are", "things", "?"))

chain.getVec("hello", 6)
// Vector[String] = Vector(hello, marcin, how, are, things, ?)
```

## Using chain with random number generator.

```
Some methods require providing RNG instance.

val rng = RNG(123) // initialize with seed, or
val rng = RNG.init // initialize with system time as seed

chain.getVecWithProb(rng)("hello", 6)
// (markov4s.RNG, Vector[String]) = (RNG@223576932655868,Vector(hello, world, how, are, things, ?))


val result = (0 to 10).scanLeft((RNG.init, Vector[String]()))((a, b) => chain.getVecWithProb(a._1)("hello", 6))

// or use utility method
val result = MarkovChain.sequenceVec(RNG.init, 10, 6, "hello", chain.getVecWithProb _)

result._2.foreach(x => println(x.mkString(" ")))

// result:
hello marcin how are you !
hello world how are things ?
hello marcin how are things ?
hello world how are things ?
hello marcin how are things ?
hello marcin how are you !
hello world how are things ?
hello marcin how are you !
hello marcin how are things ?
hello world how are things ?

val result = MarkovChain.sequenceRandomVec(RNG.init, 10, 6, chain.getRandomVecWithProb _)

result._2.foreach(x => println(x.mkString(" ")))

// result:
hello world how are things ?
world how are things ?
how are things ?
world how are you !
world how are things ?
are you !
how are you !
are you !
you !
are things ?
are things ?
```
