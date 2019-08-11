package org.vaslabs.releases

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.ContainerConfig

class DockerTestRunner(implicit docker: DockerClient) {


  def runTest(
              image: String,
              command: Seq[String] = Seq("sbt", "test"),
              network: Option[String]): Unit = {
    val container = ContainerConfig.builder()
      .image(image)
      .cmd(command: _*)
      .build()
    val creation = docker.createContainer(container)

    val containerId = creation.id()
    println(s"Container id to test is ${containerId}")
    val info = docker.inspectContainer(containerId)
    println(s"Container created ${info}")

    network.foreach {
      n => docker.connectToNetwork(containerId, n)
    }

    docker.startContainer(info.config().hostname())

    if (docker.waitContainer(containerId).statusCode() != 0)
      throw new AssertionError("Regression test failed")
  }
}
