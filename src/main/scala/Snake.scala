import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import akka.actor.Status.{Failure, Success}

object Snake {
  trait Direction
  case object Up extends Direction
  case object Down extends Direction
  case object Left extends Direction
  case object Right extends Direction

  case object State
  case class CurrentState(blocks: Seq[(Int, Int)])
  case class Food(pos : (Int, Int))
  case object Move
  case object GenFood
}

class Snake extends Actor {
  import Snake._

  val random = scala.util.Random
  var currentDirection : Snake.Direction = Up
  var buffDirection = currentDirection
  var currentBlocks = genStartPositionOfSnake()
  var currentFood : (Int, Int) = (-1, -1)
  var gs : ActorRef = _

  def genStartPositionOfSnake() = {
    val p = (random.nextInt(GameState.FieldSize._1), random.nextInt(GameState.FieldSize._2))
    List((p._1, p._2), (p._1, p._2-1 % GameState.FieldSize._2), (p._1, p._2-2 % GameState.FieldSize._2))
  }

  override def receive: Receive = {
    case Down => if (currentDirection != Up) buffDirection = Down
    case Left => if (currentDirection != Right) buffDirection = Left
    case Right => if (currentDirection != Left) buffDirection = Right
    case Up => if (currentDirection != Down) buffDirection = Up
    case GameState.Subscribed => gs = sender
    case State => sender ! CurrentState(currentBlocks)
    case Food(pos) => currentFood = pos
    case GameState.Tick => move()
    case Move => move()
  }

  def move() = {
    val head = currentBlocks.head
    if (currentDirection != buffDirection) currentDirection = buffDirection
    currentDirection match {
      case Down =>
        currentBlocks = (head._1, (GameState.FieldSize._2 + head._2 - 1) % GameState.FieldSize._2) :: currentBlocks
      case Left =>
        currentBlocks = ((GameState.FieldSize._1 + head._1 - 1) % GameState.FieldSize._1, head._2) :: currentBlocks
      case Right =>
        currentBlocks = ((head._1 + 1) % GameState.FieldSize._1, head._2) :: currentBlocks
      case Up =>
        currentBlocks = (head._1, (head._2 + 1) % GameState.FieldSize._2) :: currentBlocks
    }
    if (head == currentFood) gs ! GenFood
    else currentBlocks = currentBlocks.dropRight(1)

    if (currentBlocks.distinct.size == currentBlocks.size) sender ! Success
    else sender ! Failure
  }
}
