import akka.actor.Status.Failure
import akka.actor.{Actor, ActorRef, PoisonPill}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

object GameState {
  val FieldSize = (32, 16)
  val RedrawTime : Timeout = 120
  val MovementTime : Timeout = 400

  case object Subscribe
  case object Subscribed
  case object StartGame
  case object Tick
  case class FieldState(state: Set[(Int, Int)], food: (Int, Int))
}

class GameState extends Actor {
  import GameState._

  import concurrent.ExecutionContext.Implicits.global

  implicit val timeout : Timeout = 1.second
  var subscribers : Set[ActorRef] = Set()
  var allPositions : Set[(Int, Int)] = Set()
  val random = scala.util.Random
  var food : (Int, Int) = _

  def genFood() : (Int, Int) = {
    var gen = (random.nextInt(GameState.FieldSize._1), random.nextInt(GameState.FieldSize._2))
    while (allPositions.contains(gen))
      gen = (random.nextInt(GameState.FieldSize._1), random.nextInt(GameState.FieldSize._2))
    subscribers foreach(_ ! Snake.Food(gen))
    gen
  }
  
  def sendStates(ret: ActorRef) = {
    Future.sequence(subscribers map (_ ? Snake.State)).onComplete {
      result => {
        result.get.map(state => state.asInstanceOf[Snake.CurrentState])
        allPositions = result.get.map(state => state.asInstanceOf[Snake.CurrentState]).flatMap(x => x.blocks)
        ret ! FieldState(allPositions, food)
      }
    }
  }
  
  override def receive: Receive = {
    case x : Snake.Direction => subscribers foreach(_ ! x)

    case Snake.GenFood => food = genFood()
    case Subscribe => subscribers += sender; sender ! Subscribed
    case StartGame =>
      context.system.scheduler.schedule(0.second, MovementTime.duration) {
        subscribers foreach(_ ! Tick)
      }
      food = genFood()
      self ! Snake.State
    case Snake.State => sendStates(sender)
    case Snake.Move => subscribers foreach(_ ! Snake.Move)
    case Failure => subscribers -= sender; sender ! PoisonPill
    case _ =>
  }
}
