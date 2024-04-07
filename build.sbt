name := """loginLogout"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.13.13"



// Play Framework dependencies
libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-slick" % "5.3.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.3.0",
  "mysql" % "mysql-connector-java" % "8.0.28",
  "com.stripe" % "stripe-java" % "20.91.0",
  "org.apache.kafka" %% "kafka" % "3.0.0",
  "ch.qos.logback" % "logback-classic" % "1.2.6"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
