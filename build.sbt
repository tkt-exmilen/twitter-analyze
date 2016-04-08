name := """twitter-analyze"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test,
  "org.twitter4j" % "twitter4j-core" % "4.0.4",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "com.github.seratch" % "awscala_2.11" % "0.5.+",
  "com.amazonaws" % "amazon-kinesis-client" % "1.6.1",
  "commons-configuration" % "commons-configuration" % "1.10"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
