
# Markov4s [![Build Status](https://travis-ci.org/markosski/cloudsync.svg?branch=master)](https://travis-ci.org/markosski/markov4s) [ ![Download](https://api.bintray.com/packages/markosski/maven/markov4s/images/download.svg) ](https://bintray.com/markosski/maven/markov4s/_latestVersion)

Markov4s is a small library that implements MarkovChain data structure.

Package location:
https://bintray.com/markosski/maven/markov4s

Usage:

```
import markov4s._

// empty chain
val chain = MarkovChain[Int]

val chain1 = chain + (0 -> 1) + (1 -> 2) + (2 -> 3)

// get sequence of items starting with item 0, return at most 4 items
chain1.getSeq(0, 4)

// result: Vector(0, 1, 2, 3) // unsurprisingly
```

## Join chains

```
val chain1 = MarkovChain[Int] + (0 -> 1) + (1 -> 2)
val chain2 = MarkovChain[Int] + (2 -> 3)

(chain1 ++ chain2).getSeq(0, 4)

// result: Vector(0, 1, 2, 3)
```

## Creating chain from sequence.

```
val chain = MarkovChain[String].
    fromSeq(List("hello", "world", "how", "are", "you", "!")).
    fromSeq(List("hello", "marcin", "how", "are", "things", "?"))

chain.getSeq("hello", 6)
// Vector[String] = Vector(hello, marcin, how, are, things, ?)
```

## Using chain with using random number generator.

```
Some methods require providing instance of random number generator RandLike.

val rand0 = new Rand(100) // initialize with seed, or
val rand1 = Rand() // initialize without seed

chain.getSeqWithProb(Rand())("hello", 6)
// Vector[String] = Vector(hello, world, how, are, things, ?)

val result = (0 to 10).scanLeft(Vector[String]())((a, b) => chain.getSeqWithProb(Rand())("hello", 6))

// or use utility method
val result = MarkovChain.sequence(10, 6, "hello", chain.getSeqWithProb(Rand()))

result.foreach(x => println(x.mkString(" ")))

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

val result = MarkovChain.sequenceRandom(10, 6, chain.getRandomSeqWithProb(Rand()))

result.foreach(x => println(x.mkString(" ")))

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
