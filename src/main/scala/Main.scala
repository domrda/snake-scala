import akka.actor.{ActorRef, ActorSystem}
import akka.actor.Status.{Failure, Success}

import scala.concurrent.Await
import scala.swing._
import scala.swing.event._
import akka.util.Timeout
import akka.pattern.ask
import concurrent.duration._

object Main extends SimpleSwingApplication {
  import java.awt.{Color => AWTColor}
  import javax.swing.{AbstractAction, Timer => SwingTimer}

  import event.Key._

  val mainPanelSize = new Dimension(1080, 544)
  val bluishGray = new AWTColor(48, 99, 99)
  val bluishLigtherGray = new AWTColor(79, 130, 130)
  val bluishEvenLigther = new AWTColor(145, 196, 196)
  val bluishSilver = new AWTColor(210, 255, 255)
  val red = new AWTColor(217, 74, 105)
  val blockSize = 32
  val blockMargin = 1

  implicit val system = ActorSystem("LocalSystem")
  implicit val timeout : Timeout = 10.second
  val gs = system.actorOf(akka.actor.Props[GameState], "GS")
  0 until 10 foreach(_ => gs.tell(GameState.Subscribe, system.actorOf(akka.actor.Props[Snake])))
  gs ! GameState.StartGame

  override def top: Frame = new MainFrame {
    title = "snake"
    contents = mainPanel
  }

  def mainPanel = new Panel {
    preferredSize = mainPanelSize
    focusable = true
    listenTo(keys)

    reactions += {
      case KeyPressed(_, key, _, _) =>
        onKeyPress(key)
        repaint()
    }

    override def paint(g: Graphics2D) {
      g setColor bluishGray
      g fillRect (0, 0, size.width, size.height)
      onPaint(g)
    }

    val redrawTimer = new SwingTimer(50, new AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) {
        repaint()
      }
    })

    val snakeMovementTimer = new SwingTimer(150, new AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) {
        moveSnake()
      }
    })

    redrawTimer.start()
    snakeMovementTimer.start()

    def moveSnake() : Unit = {
      gs ! Snake.Move
//      Await.result(snake ? Snake.Move, timeout.duration) match {
//        case Success =>
//        case Failure =>
//          redrawTimer.stop()
//          snakeMovementTimer.stop()
//          println("Stop")
////          isEnded = true
//          repaint()
//      }
    }
  }

  def onKeyPress(key: Key.Value) : Unit = key match {
    case Left  => gs ! Snake.Left
    case Right => gs ! Snake.Right
    case Up    => gs ! Snake.Up
    case Down  => gs ! Snake.Down
    case _ =>
  }

  def onPaint(g: Graphics2D) {
    Await.result(gs ? Snake.State, timeout.duration) match {
      case GameState.FieldState(blocks, food) =>
        drawBoard(g, (6, 6), (GameState.FieldSize._1, GameState.FieldSize._2), blocks.toSeq, food)
      case _ => println("Got something strange")
    }
  }

  def drawBoard(g: Graphics2D, offset: (Int, Int), gridSize: (Int, Int),
                blocks: Seq[(Int, Int)], food: (Int, Int)) {
    def buildRect(pos: (Int, Int)): Rectangle =
      new Rectangle(offset._1 + pos._1 * (blockSize + blockMargin),
        offset._2 + (gridSize._2 - pos._2 - 1) * (blockSize + blockMargin),
        blockSize, blockSize)
    def drawEmptyGrid() {
      g setColor bluishLigtherGray
      for {
        x <- 0 to gridSize._1 - 1
        y <- 0 to gridSize._2 - 1
        pos = (x, y)
      } g draw buildRect(pos)
    }
    def drawBlocks() {
      g setColor bluishEvenLigther
      blocks filter {_._2 < gridSize._2} foreach { b =>
        g fill buildRect(b) }
    }
    def drawCurrent() {
      g setColor bluishSilver
      g fill buildRect(food)
    }
    drawEmptyGrid()
    drawCurrent()
    drawBlocks()
//    if (isEnded) {
//      g setColor red
//      g fill buildRect(blocks.head)
//    }
  }
}
