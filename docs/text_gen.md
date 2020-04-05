---
layout: page
title: "Text Generation"
section: "usage"
position: 2
---

# Generating Lines from Hamlet

```scala mdoc:silent
import markov4s._
import scala.io.Source

val text = Source.fromFile("docs/resources/hamlet.txt")

// Some cleanup
val data = text.getLines.toList.flatMap { l => l.split(" ").flatMap { x => 
    if      (x.endsWith(",")) List(x.stripSuffix(","), ",") 
    else if (x.endsWith(".")) List(x.stripSuffix("."), ".")
    else    List(x) }
    }.asInstanceOf[List[String]]

// Create chain
val chain = MarkovChain[String].fromSeq(data)
```

```scala mdoc
implicit val rand = Rand()

chain.getRandomSeqWithProb(100).mkString(" ").replace(" ,", ",").replace(" .", ".")

chain.getRandomSeqWithProb(100).mkString(" ").replace(" ,", ",").replace(" .", ".")

chain.getRandomSeqWithProb(100).mkString(" ").replace(" ,", ",").replace(" .", ".")
```
