package org.vaslabs.releases

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import com.spotify.docker.client.DockerClient
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.{Failure, Success, Try}

class DockerTestRunnerSpec extends
      FlatSpec with
      Matchers with
      BeforeAndAfterAll {
  import com.spotify.docker.client.DefaultDockerClient
  implicit lazy val docker = DefaultDockerClient.fromEnv.build
  val dockerImageId = new AtomicReference[String]
  override def beforeAll(): Unit = {
    dockerImageId.set(createTestImage)
  }

  override def afterAll(): Unit = docker.close()

  def createTestImage(implicit dockerClient: DockerClient): String = {
    dockerClient.build(
      new File(".").toPath,
    )
  }




  "docker test runner" must "raise error when test fails" in {

    val testRunner = new DockerTestRunner()
    Try(testRunner.runTest(
      dockerImageId.get(), Seq("sbt", "testOnly *SampleFailingTest"), None
    )) should matchPattern {
      case Failure(_: AssertionError) =>
    }
  }

  "docker test runner" must "succeed" in {

    val testRunner = new DockerTestRunner()
    Try(testRunner.runTest(
      dockerImageId.get(),
      Seq("sbt", "testOnly *SampleSucceedingTest"), None
    )) should matchPattern {
      case Success(_) =>
    }
  }
}
