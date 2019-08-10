name := "sbt-regression-suite"

version := "0.1"

Global / scalaVersion := "2.12.9"

lazy val `regression-suite` = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-regression-suite"
  )

libraryDependencies ++= Seq(
  "com.spotify" % "docker-client" % "8.14.3",
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  Defaults.sbtPluginExtra()
)
