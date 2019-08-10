package sbtregressionsuite

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import org.vaslabs.releases.DockerTestRunner
import sbt._

trait SbtRegressionSuiteKeys {
  val dockerImage = settingKey[String]("The docker image where the regression test is")
  val currentVersion = settingKey[String](
    "The version of the docker image that contains the test (backwards compatibility)"
  )
  val testCommand = settingKey[Seq[String]](
    "The command to run when testing"
  )

  val newVersion = settingKey[String]("The version to test forwards compatibility")
  val updateLatest = settingKey[Boolean]("Update the latest tag once backwards and forwards tests pass")

  val regressionTest = taskKey[Unit]("Runs backwards compatibility tests and then forwards. Updates the latest tag if specified and tests pass")
}
object SbtRegressionSuiteKeys extends SbtRegressionSuiteKeys {
  lazy val baseRegressionSuiteSettings: Seq[Def.Setting[_]] = Seq(
    regressionTest := {
      RegressionSuite.fullTest(
        (dockerImage in regressionTest).value,
        (testCommand in regressionTest).value,
        (currentVersion in regressionTest).value,
        (newVersion in regressionTest).value,
        (updateLatest in regressionTest).value
      )
    },
    updateLatest := true,
    currentVersion := "latest",
    testCommand := Seq("sbt", "test")
  )

}

object RegressionSuite {
  def fullTest(
     image: String,
     command: Seq[String],
     currentVersion: String,
     newVersion: String,
     updateLatest: Boolean) = {
    implicit val docker = DefaultDockerClient.fromEnv.build
    implicit val dockerTestRunner = new DockerTestRunner()
    backwardsCompatibilityTest(image, command, currentVersion)
    forwardsCompatibilityTest(image, command, newVersion)
    updateLatestImage(image, newVersion, updateLatest)
  }

  private def backwardsCompatibilityTest(
                                          image: String,
                                          command: Seq[String],
                                          version: String)(implicit dockerTestRunner: DockerTestRunner) = {
    println("Running backwards compatibility test")
    singleTest(image, command, version)
  }

  private def forwardsCompatibilityTest(
        image: String,
        command: Seq[String],
        version: String)(implicit dockerTestRunner: DockerTestRunner) = {
    println("Running forwards compatibility test")
    singleTest(image, command, version)
  }

  private def singleTest(
                          image: String,
                          command: Seq[String],
                          version: String)(implicit
                          dockerTestRunner: DockerTestRunner) = {
    dockerTestRunner.runTest(s"${image}:${version}", command)
  }

  private def updateLatestImage(image: String, newVersion: String, update: Boolean)(implicit dockerClient: DockerClient) = {
    if (update) {
      println("Updating latest image")
      dockerClient.tag(s"${image}:${newVersion}", s"$image:latest")
      dockerClient.push(s"$image:latest")
    } else {
      println("Latest image update skipped")
    }
  }
}