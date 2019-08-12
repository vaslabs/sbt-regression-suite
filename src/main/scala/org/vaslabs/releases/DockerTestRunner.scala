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
