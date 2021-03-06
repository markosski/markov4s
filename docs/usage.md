---
layout: page
title: "Basic Usage"
section: "usage"
position: 1
---


# Basic Usage

```scala mdoc
import markov4s._

val chain = MarkovChain[String]
val chain1 = chain + ("a" -> "b") + ("b" -> "c") + ("c" -> "d")

// get sequence of items starting with item "a", return at most 4 items
chain1.getSeq("a", 4)
```

## Join chains

```scala mdoc
val chain2 = MarkovChain[String] + ("a" -> "b") + ("b" -> "c")
val chain3 = MarkovChain[String] + ("c" -> "d")

(chain2 ++ chain3).getSeq("a", 4)
```

## Create from sequence

```scala mdoc
val chain4 = MarkovChain[String]
    .fromSeq(List("hello", "world", "how", "are", "you"))
    .fromSeq(List("hello", "Susan", "how", "are", "things"))
    .fromSeq(List("hello", "John", "how", "is", "life"))
    .fromSeq(List("hello", "John", "how", "do", "you", "do"))

chain4.getSeq("hello", 6)
```

As you can see from the output, new sentence was build using words with highest occurance.
We can randomize this where next selected words will be chosen based on their probability to occure.

```scala mdoc
implicit val rand = Rand()

chain4.getSeqWithProb("hello", 6)
chain4.getSeqWithProb("hello", 6)
chain4.getSeqWithProb("hello", 6)
```
Note: This documentation is generated which may cause some code evaluations return different result.
