name := "onestopweb"

scalaVersion := "2.11.5"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.mindrot" % "jbcrypt" % "0.3m",
  "mysql" % "mysql-connector-java" % "5.1.18"
)     

