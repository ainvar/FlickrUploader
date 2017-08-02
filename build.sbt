name := "FlickrUploader"

version := "1.0"

scalaVersion := "2.12.1"
lazy val akkaVersion = "2.5.2"

libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.102-R11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalafx" %% "scalafx" % "8.0.102-R11",
  "org.scalikejdbc" %% "scalikejdbc"       % "3.0.0",
  "com.h2database"  %  "h2"                % "1.4.195",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  "com.typesafe.play" %% "play-ws" % "2.6.2",
  "com.aetrion.flickr" % "flickrapi" % "1.1"
)

fork in run := true
scalacOptions += "-deprecation"

mainClass in assembly := Some("Main")


