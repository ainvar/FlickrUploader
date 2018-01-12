name := "FlickrUploader"

version := "1.0"

scalaVersion := "2.12.2"
lazy val akkaVersion = "2.5.6"

libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.102-R11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalafx" %% "scalafx" % "8.0.102-R11",
  "org.scalikejdbc" %% "scalikejdbc"       % "3.0.0",
  "com.h2database"  %  "h2"                % "1.4.195",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.typesafe.play" %% "play-ws" % "2.6.2",
  "com.flickr4java" % "flickr4java" % "2.11"
)

fork in run := true

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-language:higherKinds" // Enable the use of higher kinds by default
)

mainClass in assembly := Some("Main")


