import sbt._
import Keys._

object HelloBuild extends Build {

  lazy val rootApp = Project(id = "root",
    base = file(".")) aggregate(client, server, messages) dependsOn(client, server, messages)

  lazy val client = Project(id = "client",
    base = file("client")) aggregate(messages) dependsOn(messages)

  lazy val server = Project(id = "server",
    base = file("server")) aggregate(messages) dependsOn(messages)

  lazy val messages = Project(id = "messages",
    base = file("messages"))
}