lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "markosski",
      scalaVersion := "2.12.11",
      version      := "0.2.0"
    )),
    bintrayReleaseOnPublish in ThisBuild := false,
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    name := "markov4s",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5"
  )
