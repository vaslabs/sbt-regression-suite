import sbt.url
import xerial.sbt.Sonatype.GitHubHosting

name := "sbt-regression-suite"

version := "1.0"

Global / scalaVersion := "2.12.9"

lazy val publishSettings = Seq(
  publishTo := Some(
    if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
    else Opts.resolver.sonatypeStaging
  ),
  organization := "org.vaslabs.tests",
  organizationName := "Vasilis Nicolaou",
  sonatypeProfileName := "org.vaslabs",
  sonatypeProjectHosting := Some(GitHubHosting("vaslabs", "sbt-regression-suite", "vaslabsco@gmail.com")),
    scmInfo := Some(ScmInfo(
    url("https://github.com/vaslabs/sbt-regression-suite"),
    "scm:git@github.com:vaslabs/sbt-regression-suite.git")),
  developers := List(
    Developer(
      id    = "vaslabs",
      name  = "Vasilis Nicolaou",
      email = "vaslabsco@gmail.com",
      url   = url("http://vaslabs.org")
    )
  ),
  publishMavenStyle := true,
  licenses := List("Apache-2.0" -> new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://git.vaslabs.org/vaslabs/sbt-regression-suite")),
  startYear := Some(2018)
)


lazy val pluginSettings = Seq(
  sbtPlugin := true,
  crossSbtVersions := Seq("1.2.8")
)


lazy val `regression-suite` = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin, SbtPlugin)
  .settings(pluginSettings)
  .settings(
    name := "sbt-regression-suite"
  ).settings(publishSettings)
  .settings(releaseSettings)


libraryDependencies ++= Seq(
  "com.spotify" % "docker-client" % "8.14.3",
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

lazy val releaseSettings = {
  import ReleaseTransformations._

  Seq(
    releaseCrossBuild := false,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("^ scripted"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("^ publishSigned"),
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    )
  )
}