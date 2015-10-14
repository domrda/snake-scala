name := "snake-scala"

version := "1.0"

scalaVersion := "2.11.0-M7"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val akkaVersion = "2.2.3"
  Seq(
    "org.scala-lang" % "scala-swing" % "2.11.0-M7",
    "com.typesafe.akka" %% "akka-actor" % "2.2.3"
  )
}
