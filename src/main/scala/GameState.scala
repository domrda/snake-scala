

object GameState {
  trait Direction
  case object Up extends Direction
  case object Down extends Direction
  case object Left extends Direction
  case object Right extends Direction
  case object State
  case class CurrentState(blocks: Seq[(Int, Int)], food: (Int, Int))
  case object Move
}

class GameState {
  import GameState._

  var currentDirection : GameState.Direction = Up
  var currentBlocks = List((1, 3), (1, 2), (1,1))
  var currentFood : (Int, Int) = (5, 5)
  val random = scala.util.Random

  def genFood() : (Int, Int) = {
    var gen = (random.nextInt(16), random.nextInt(16))
    while (currentBlocks.contains(gen))
      gen = (random.nextInt(16), random.nextInt(16))
    gen
  }

  def receive(dir : Direction) = dir match {
    case Down => if (currentDirection != Up) currentDirection = Down
    case Left => if (currentDirection != Right) currentDirection = Left
    case Right => if (currentDirection != Left) currentDirection = Right
    case Up => if (currentDirection != Down) currentDirection = Up
  }

  def getGameState = {
    CurrentState(currentBlocks, currentFood)
  }

  def move() : Boolean = {
    val head = currentBlocks.head
    currentDirection match {
      case Down =>
        currentBlocks = (head._1, head._2 - 1) :: currentBlocks
      case Left =>
        currentBlocks = (head._1 - 1, head._2) :: currentBlocks
      case Right =>
        currentBlocks = (head._1 + 1, head._2) :: currentBlocks
      case Up =>
        currentBlocks = (head._1, head._2 + 1) :: currentBlocks
    }
    if (head == currentFood) currentFood = genFood()
    else currentBlocks = currentBlocks.dropRight(1)

    (currentBlocks.count(pair => pair._1 < 0 || pair._2 < 0 || pair._1 >= 16 || pair._2 >= 16) > 0) ||
      (currentBlocks.distinct.size != currentBlocks.size)
  }
}
