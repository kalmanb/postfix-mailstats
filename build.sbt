organization := "com.kalmanb.mailstats"

scalaVersion := "2.10.3"

lazy val agent = project.in(file("agent"))
  .settings(
    libraryDependencies ++= Dependencies.agentDeps
  )

lazy val backend = project.in(file("backend"))
  .settings(
    libraryDependencies ++= Dependencies.backendDeps
  )




