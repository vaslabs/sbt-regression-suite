package org.vaslabs.sampletest

import org.scalatest.{FlatSpec, Ignore, Matchers}

class SampleSucceedingTest extends FlatSpec with Matchers{

  "test" must "succeed" in {
    1 shouldBe 1
  }

}
