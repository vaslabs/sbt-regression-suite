package sbtregressionsuite

import sbt.AutoPlugin

object SbtRegressionSuitePlugin extends AutoPlugin {

  override def trigger = noTrigger

  override def requires = sbt.plugins.JvmPlugin

}
