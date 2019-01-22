
Markov4s is a small library that introduces immutable data structure to represent Markov Chain.

Usage:

```
import markov4s._

// empty chain
val chain = MarkovChain[Int]

// add relationship between node of value 0 and 1
val chain1 = chain + (0 -> 1) + (1 -> 2) + (2 -> 3)
val chain2 = chain1.getVec(0, 3)

// result: Vector(0, 1, 2, 3)
```

## Creating chain from sequence.

```
val chain = MarkovChain[String].
    fromSeq(List("hello", "world", "how", "are", "you", "!")).
    fromSeq(List("hello", "marcin", "how", "are", "things", "?"))

for (i <- 0 until 10) println(chain.getVecWithProb("hello", 5).mkString(" "))

// Will result in the following output.

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
```
