name := "snake-client"

version := "1.0"

scalaVersion := "2.10.6"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= {
  val akkaVersion = "2.3.9"
  val sprayVersion = "1.3.1"
  Seq(
    "org.scala-lang" % "scala-swing" % "2.10.6",
    "com.typesafe.akka" % "akka-actor_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-remote_2.10" % akkaVersion
  )
}
