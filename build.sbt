lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "markosski",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "markov4s",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5"
  )
