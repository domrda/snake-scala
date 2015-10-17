package server
import messages._

import akka.actor._

class Connector(val games: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case Messages.Connect(uid) =>
      println("Connect from ", sender())
      val connection = context.system.actorOf(Props(classOf[Connection], sender, uid, games))
      sender ! Messages.Connected(connection)
  }
}

class Connection(val remote: ActorRef, val uid: String, val games: ActorRef) extends Actor with ActorLogging {
  context.watch(remote)
  println("Connection of " + self + "with " + remote)

  override def receive: Actor.Receive = {
    case Terminated => context.stop(self)
    case x : Messages.FindGame =>
      println("FindGame from ", sender())
      games.forward(x)
    case _ => println("Something strange come to Connection")
  }
}