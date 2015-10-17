package Server
import messages._

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import spray.http.{HttpEntity, HttpResponse}
import spray.routing.HttpService

import scala.concurrent.duration._

class WebUidGenerator(games: ActorRef) extends HttpService with Actor {
  def actorRefFactory = context
  implicit val timeout: Timeout = 2.second

  override def receive = runRoute(route)

  lazy val route = {
    path("") {
      get {
        complete {
          HttpResponse(entity = HttpEntity(generatePair()))
        }
      }
    }
  }

  def generatePair() : String = {
//    val player1 = UUID.randomUUID.toString
//    val player2 = UUID.randomUUID.toString
    val player1 = "59b54598-ef38-4995-b659-fbea3ecbe317"
    val player2 = "23523a7b-af9e-4fab-8703-bcbdb03e0071"
    games ! Messages.Pair(player1, player2)
    "Player1: " + player1 + "\nPlayer2: " + player2
  }
}
