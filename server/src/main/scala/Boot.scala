import Server.WebUidGenerator
import akka.io.IO
import server.{Games, Connector}
import akka.actor.{ActorSystem, Props}
import spray.can.Http

object Boot extends App {
  implicit val system = ActorSystem("server")
  val games = system.actorOf(Props[Games])
  val connector = system.actorOf(Props(classOf[Connector], games), "Connector")
  val web = system.actorOf(Props(classOf[WebUidGenerator], games))
  IO(Http) ! Http.Bind(
    web,
    interface = "localhost",
    port = 9898
  )
}
