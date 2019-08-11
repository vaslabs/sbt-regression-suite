package org.vaslabs.releases


import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.{Success, Try}

class DockerRegressionPackerSpec extends FlatSpec with
  Matchers with
  BeforeAndAfterAll {
  import com.spotify.docker.client.DefaultDockerClient
  implicit lazy val docker = DefaultDockerClient.fromEnv.build

  override def afterAll(): Unit = docker.close()

  "packer" must "pack a project into a docker image" in {
    Try(
      new DockerRegressionPacker().packRegressionTest(
        "regressionsuite",
        "testversion",
        "2.12.9",
        "1.2.8",
        "sbt-regression-suite"
      )
    ) should matchPattern {
      case Success(_) =>
    }
  }

}
