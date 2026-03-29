val scala3Version = "3.8.2"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalafixPlugin)
  .settings(
    semanticdbEnabled := true,
    scalacOptions += "-Wunused:all",
    name := "lisp2c",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.2.4" % Test
  )
