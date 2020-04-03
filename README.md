
# Markov4s [![Build Status](https://travis-ci.org/markosski/cloudsync.svg?branch=master)](https://travis-ci.org/markosski/markov4s) [ ![Download](https://api.bintray.com/packages/markosski/maven/markov4s/images/download.svg) ](https://bintray.com/markosski/maven/markov4s/_latestVersion)

Markov4s is a small library that implements MarkovChain data structure.

```scala
import markov4s._

val chain = MarkovChain[String]
// chain: MarkovChain[String] = MarkovChain(Map())
val chain1 = chain + ("a" -> "b") + ("b" -> "c") + ("c" -> "d")
// chain1: MarkovChain[String] = MarkovChain(
//   Map("a" -> Map("b" -> 1), "b" -> Map("c" -> 1), "c" -> Map("d" -> 1))
// )

// get sequence of items starting with item "a", return at most 4 items
chain1.getSeq("a", 4)
// res0: Vector[String] = Vector("a", "b", "c", "d"))
```

For more unsage and example please see documentation at https://markosski.github.io/markov4s