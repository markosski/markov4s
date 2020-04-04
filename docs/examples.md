---
layout: page
title: "Examples"
section: "examples"
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
chain.getRandomSeqWithProb(100)
    .mkString(" ")
    .replace(" ,", ",")
    .replace(" .", ".")
```


Selected output
---------------

`
Would have hitherto conceal'd this Hyperion to my tongue! I am too solid flesh would not think on't! ah, Nor shall in heaven Or that wants discourse of most wicked speed, Horatio! My father's brother, No, madam, and resolve itself should come to post With such dexterity to this! But I remember? Why, it is woman! A little month, most wicked speed, Thaw, Let me. I am very glad to drink deep ere you. Would I shall in your silence still; And what is your own report Against yourself
`