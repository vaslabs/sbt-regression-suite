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
