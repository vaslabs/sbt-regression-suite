# Regression suite plugin

## Features:
- Pack your regression tests
- Automates backwards and forwards compatibility tests for your releases


## The general idea

### Pre-requisites:

- You have an integration test that uses your production code case classes and automatic derivation (e.g. for
json http requests).

- The integration test is pointing to a service that is using your latest code. If your integration
tests are testing your code in the repository this is not going to work for now as there are no mechanics yet with code version control, so you have to ensure that you are not testing the same version of production code.

In other words, you need to be testing a service with state A with 2 different versions of your integration test.


This plugin packs your code in a docker container and does some simple version control over it. For instance, the
latest tag has your currently running production code.

Everytime you are doing a release you can do
```
regression:test
```
1. With the default settings, it will run the tests from the latest docker image. If they pass, your code is
backwards compatible.
2. Then it packs a new docker container with your new code and runs the tests again. If they pass, your code
is forwards compatible.
3. If both tests are successful, the latest tag is updated with a docker push.


### Usage

1. Add the plugin to your project

```
addSbtPlugin("org.vaslabs.tests" % "sbt-regression-suite" % "1.0")
```

2. Enable the plugin in your integration test project.
```
  .enablePlugins(RegressionSuitePlugin)
```
3. Configure the plugin

```

dockerImage in regression := "The base name of your docker image without the tag"

newVersion in regression := version.value //This is going to be the tag of the new version

testCommand in regression := Seq("sbt" ,"schedulerIntegrationTests/test")

//This is useful if you spin the service with docker-compose (e.g. locally)
dockerNetwork in regression := Some("sandbox_scheduler")

```

4. First time use:
```
regression:pack
```
Find the docker image from the logs and do
```
docker tag <image:version> <image:latest>
```
Run your first regression
```
regression:test
```


This is a PoC, please send feedback or ideally pull requests thanks :)
