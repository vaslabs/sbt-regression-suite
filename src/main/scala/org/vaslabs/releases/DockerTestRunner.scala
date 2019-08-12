/*
 * Copyright 2018 Vasilis Nicolaou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
