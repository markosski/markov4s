
lazy val root = (project in file("."))
  .enablePlugins(MicrositesPlugin, SiteScaladocPlugin, GhpagesPlugin)
  .settings(
    inThisBuild(List(
      organization := "marcinkossakowski.com",
      scalaVersion := "2.12.11",
      version      := "0.2.0"
    )),
    git.remoteRepo := "git@github.com:markosski/markov4s.git",
    micrositeUrl := "https://markosski.github.io",
    // micrositeBaseUrl := "/markov4s",
    bintrayReleaseOnPublish in ThisBuild := false,
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    name := "markov4s",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5"
  )
