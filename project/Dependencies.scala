import sbt._
import Keys._

object Dependencies {

  lazy val agentDeps = Seq(
    commonsio
  ) ++ commonDeps ++ testCommonDeps

  lazy val backendDeps = Seq(
    sprayCan,
    sprayTestkit
  ) ++ commonDeps ++ testCommonDeps

  lazy val commonDeps = Seq(
    akkaActor,
    javaDriver,
    scalaz
  )

  lazy val testCommonDeps = Seq(
    akkaTestkit,
    scalatest,
    junit,
    mockito
  )

  val akkaVersion = "2.2.3"
  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  lazy val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"

  val sprayVersion = "1.2.0"
  lazy val sprayCan = "io.spray" % "spray-routing" % sprayVersion 
  lazy val sprayTestkit = "io.spray" % "spray-testkit" % sprayVersion % "test"

  lazy val javaDriver = "com.datastax.cassandra" % "cassandra-driver-core" % "2.0.0-rc2"

  lazy val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.0-M5"

  lazy val commonsio = "commons-io" % "commons-io" % "2.4"

  lazy val scalatest = "org.scalatest" %% "scalatest" % "2.1.0-RC2" % "test"
  lazy val junit = "junit" % "junit" % "4.11" % "test"
  lazy val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"
}
