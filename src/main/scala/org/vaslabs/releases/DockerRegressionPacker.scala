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
