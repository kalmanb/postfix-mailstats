organization := "com.kalmanb.mailstats"

name := "agent"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3",
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.0.0-rc2",
  "org.scalaz" %% "scalaz-core" % "7.1.0-M5",
  "commons-io" % "commons-io" % "2.4",
  "org.scalatest" %% "scalatest" % "2.1.0-RC2" % "test",
  "junit" % "junit" % "4.11" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test" 
)





