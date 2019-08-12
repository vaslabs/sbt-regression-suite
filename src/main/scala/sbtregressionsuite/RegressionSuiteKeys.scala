/*
 * Copyright (c) 2018 Vasilis Nicolaou
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package sbtregressionsuite

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import org.vaslabs.releases.{DockerRegressionPacker, DockerTestRunner}
import sbt._
import sbt.Keys._

trait RegressionSuiteKeys {
  val dockerImage = settingKey[String]("The docker image where the regression test is")
  val currentVersion = settingKey[String](
    "The version of the docker image that contains the test (backwards compatibility)"
  )
  val testCommand = settingKey[Seq[String]](
    "The command to run when testing"
  )

  val newVersion = settingKey[String](
    "The version to test forwards compatibility")
  val updateLatest = settingKey[Boolean](
    "Update the latest tag once backwards and forwards tests pass")

  val test = taskKey[Unit](
    "Runs backwards compatibility tests and then forwards. Updates the latest tag if specified and tests pass")

  val dockerNetwork = settingKey[Option[String]](
    "Network to attach the regression test, e.g. useful you are running the service locally")
  val pack = taskKey[Unit](
    "Packs your source code into a runnable docker container, ready for regression test lifecycle")

  val regression = Configuration.of("Regression", "regression")
}
object RegressionSuiteKeys extends RegressionSuiteKeys {
  lazy val baseRegressionSuiteSettings: Seq[Def.Setting[_]] = Seq(
    test in regression := {
      RegressionSuite.fullTest(
        (dockerImage in test).value,
        (testCommand in test).value,
        (currentVersion in test).value,
        (newVersion in test).value,
        (updateLatest in test).value,
        (dockerNetwork in test).value
      )
    },
    updateLatest := true,
    currentVersion := "latest",
    testCommand := Seq("sbt", "test"),
    dockerNetwork := None,
    pack in regression := {
      RegressionSuite.pack(
        (dockerImage in test).value,
        (newVersion in test).value,
        (scalaVersion in regression).value,
        (sbtVersion in Global).value,
        (name in ThisProject).value
      )
    }
  )

}

object RegressionSuite {

  def pack(image: String, version: String, scalaVersion: String, sbtVersion: String, projectName: String): Unit = {
    implicit val docker = DefaultDockerClient.fromEnv.build
    try {
      val dockerRegressionPacker = new DockerRegressionPacker()
      dockerRegressionPacker.packRegressionTest(image, version, scalaVersion, sbtVersion, projectName)
    }
    finally {
      docker.close()
    }
  }

  def fullTest(
     image: String,
     command: Seq[String],
     currentVersion: String,
     newVersion: String,
     updateLatest: Boolean,
     network: Option[String]) = {
    implicit val docker = DefaultDockerClient.fromEnv.build
    try {
      implicit val dockerTestRunner = new DockerTestRunner()
      backwardsCompatibilityTest(image, command, currentVersion, network)
      forwardsCompatibilityTest(image, command, newVersion, network)
      updateLatestImage(image, newVersion, updateLatest)
    } finally {
      docker.close()
    }
  }

  private def backwardsCompatibilityTest(
        image: String,
        command: Seq[String],
        version: String,
        network: Option[String])(implicit dockerTestRunner: DockerTestRunner) = {
    println("Running backwards compatibility test")
    singleTest(image, command, version, network)
  }

  private def forwardsCompatibilityTest(
        image: String,
        command: Seq[String],
        version: String,
        network: Option[String])(implicit dockerTestRunner: DockerTestRunner) = {
    println("Running forwards compatibility test")
    singleTest(image, command, version, network)
  }

  private def singleTest(
                          image: String,
                          command: Seq[String],
                          version: String,
                          network: Option[String])(implicit
                          dockerTestRunner: DockerTestRunner) = {
    dockerTestRunner.runTest(s"${image}:${version}", command, network)
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