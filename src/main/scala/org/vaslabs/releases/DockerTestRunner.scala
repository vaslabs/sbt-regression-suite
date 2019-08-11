package org.vaslabs.releases

import java.io.{File, FileWriter, PrintWriter}
import java.nio.file.Files

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.spotify.docker.client.DockerClient.BuildParam
import com.spotify.docker.client.messages.ContainerConfig

import scala.io.Source
import scala.util.Try

class DockerTestRunner(implicit docker: DockerClient) {


  def runTest(
              image: String,
              command: Seq[String] = Seq("sbt", "test")): Unit = {
    val container = ContainerConfig.builder()
      .image(image)
      .cmd(command: _*)
      .build()
    val creation = docker.createContainer(container)

    val containerId = creation.id()
    println(s"Container id to test is ${containerId}")
    val info = docker.inspectContainer(containerId)
    println(s"Container created ${info}")

    docker.startContainer(info.config().hostname())

    if (docker.waitContainer(containerId).statusCode() != 0)
      throw new AssertionError("Regression test failed")
  }
}
