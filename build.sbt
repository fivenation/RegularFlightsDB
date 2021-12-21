name := "flightsDB"

version := "0.1"

scalaVersion := "2.13.7"

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.2.1"

libraryDependencies += "net.liftweb" %% "lift-json" % "3.5.0"

libraryDependencies += "org.jsoup" % "jsoup" % "1.11.2"

libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "4.4.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.7"

lazy val root = (project in file("."))
  .settings(
    name := "laba_4"
  )
