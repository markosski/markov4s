import microsites.{ExtraMdFileConfig}

lazy val root = (project in file("."))
  .enablePlugins(MicrositesPlugin, SiteScaladocPlugin, GhpagesPlugin)
  .settings(
    inThisBuild(List(
      organization := "com.github.markosski",
      scalaVersion := "2.12.11",
      version      := "0.2.0"
    )),
    git.remoteRepo := "git@github.com:markosski/markov4s.git",
    micrositeUrl := "https://markosski.github.io",
    micrositeDocumentationUrl := "/markov4s/latest/api/markov4s/MarkovChain.html",
    micrositeAnalyticsToken := "UA-162748963-1",
    micrositeBaseUrl := "/markov4s",
    micrositeTwitter := "@martez81",
    micrositeTheme := "pattern",
    micrositeExtraMdFiles := Map(
      file("README.md") -> ExtraMdFileConfig(
        "index.md",
        "home",
        Map("layout" -> "home", "title" -> "Home", "section" -> "home", "position" -> "1")
      )),
    micrositeExtraMdFilesOutput := (resourceManaged in Compile).value / "jekyll",
    bintrayReleaseOnPublish in ThisBuild := false,
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    name := "markov4s",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5"
  )
