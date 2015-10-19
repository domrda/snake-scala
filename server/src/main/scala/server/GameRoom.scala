package server
import akka.actor._
import akka.util.Timeout
import messages._

import scala.concurrent.duration._

class Games extends Actor with ActorLogging {

  var games : Map[(String, String), ActorRef] = Map.empty

  override def receive: Actor.Receive = {
    case Messages.Pair(player1, player2) =>
      val room = context.system.actorOf(Props(classOf[GameRoom], player1, player2))
      games += (player1, player2) -> room
    case Messages.FindGame(player) =>
      println("FindGame from ", sender())
      val gameFound = games.find(complect => complect._1._1 == player || complect._1._2 == player).orNull
      sender ! Messages.GameFound(gameFound)
  }
}

class GameRoom(player1: String, player2: String) extends Actor with ActorLogging {
  import concurrent.ExecutionContext.Implicits.global
  implicit val timeout : Timeout = 1.second

  val random = scala.util.Random
  var cancel = context.system.scheduler.scheduleOnce(5.minutes, self, PoisonPill)
  var snakes: Set[ActorRef] = Set.empty
  var snake1: List[(Int, Int)] = List.empty
  var snake2: List[(Int, Int)] = List.empty
  var food : (Int, Int) = _

  def genFood() : (Int, Int) = {
    var gen = (random.nextInt(Games.fieldSize._1), random.nextInt(Games.fieldSize._2))
    while (snake1.contains(gen) || snake2.contains(gen))
      gen = (random.nextInt(Games.fieldSize._1), random.nextInt(Games.fieldSize._2))
    snakes foreach(_ ! Snake.Food(gen))
    println("GenFood: " + gen)
    gen
  }

  override def receive: Receive = {
    case Messages.PlayerConnected(uid) =>
      println("PlayerConnected from ", sender())
      if (uid == player1 || uid == player2) {
        cancel.cancel()
        val snake = context.system.actorOf(Props(classOf[Snake], uid))
        snakes += snake
        sender ! Messages.SnakeCreated(snake)
        food = genFood()
        if (snakes.size >= 2)
          println("Start game!")
          context.system.scheduler.scheduleOnce(10.minutes, self, PoisonPill)
          context.system.scheduler.schedule(10.seconds, 250.millis) {
          snakes.foreach(_ ! Snake.Move)
        }
        context.watch(snake)
      }
    case Snake.AteItself(player) =>
      if (player == player1)
        snake1 = List.empty
      else
        snake2 = List.empty
      println("Snake ate itself")
      snakes -= sender; sender ! PoisonPill
    case Snake.AteFood =>
      food = genFood()
    case Messages.GetState => sender ! Messages.State(snake1, snake2, food)
    case Snake.SnakeState(pos, player) =>
      if (player == player1)
        snake1 = pos
      else
        snake2 = pos
    case x: Terminated => println("Snake died")//context.stop(self)
    case other => println("Something strange come to GameRoom: " + other)
  }
}
