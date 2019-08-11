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
    } finally {
      Files.delete(dockerFile)
    }
  }

}
