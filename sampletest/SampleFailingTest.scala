package org.vaslabs.sampletest

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random

class SampleFailingTest extends FlatSpec with Matchers{

  "test" must "fail" in {
    Random.nextString(32) shouldBe "nochance"
  }

}
