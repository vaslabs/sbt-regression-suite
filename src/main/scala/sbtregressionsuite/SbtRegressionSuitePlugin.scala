package sbtregressionsuite

import sbt._

object SbtRegressionSuitePlugin extends AutoPlugin {

  override def trigger = noTrigger

  override def requires = sbt.plugins.JvmPlugin

  override val projectSettings =
      inConfig(SbtRegressionSuiteKeys.regression)(SbtRegressionSuiteKeys.baseRegressionSuiteSettings)
}
