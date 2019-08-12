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

import java.io.{File, FileWriter, PrintWriter}
import java.nio.file.Files

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.DockerClient.BuildParam
import com.spotify.docker.client.messages.ProgressMessage

import scala.io.Source

class DockerRegressionPacker(implicit docker: DockerClient) {

  def packRegressionTest(
                          targetImage: String,
                          version: String,
                          scalaVersion: String,
                          sbtVersion: String,
                          projectName: String
                        ): Unit = {
    val dockerFileContents =
      Source.fromResource("DockerfileTemplate", this.getClass.getClassLoader).mkString.replace(
        "$$SCALA$$", scalaVersion
      ).replace("$$SBT$$", sbtVersion)
        .replace("$$PROJECTNAME$$", projectName)
    val dockerFileTemp = Files.createTempFile("Dockerfile.", "")
    val printWriter = new PrintWriter(new FileWriter(dockerFileTemp.toFile))
    printWriter.write(dockerFileContents)
    printWriter.close()
    val dockerFile = Files.copy(dockerFileTemp, {dockerFileTemp.getFileName})
    try {
      println(s"Image definition in ${dockerFile.toAbsolutePath.toString}")
      val imageId = docker.build(new File(".").toPath,
        (message: ProgressMessage) => println(message),
        BuildParam.dockerfile(dockerFile))
      println(s"Packed in docker image ${imageId}")
      println(s"Tagging $imageId $targetImage:$version")
      docker.tag(imageId, s"$targetImage:$version")
    } finally {
      Files.delete(dockerFile)
    }
  }

}
