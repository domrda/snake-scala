package client

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import messages._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.swing._
import scala.swing.event.{Key, KeyPressed}

object Client extends SimpleSwingApplication {
  import java.awt.{Color => AWTColor}
  import javax.swing.{AbstractAction, Timer => SwingTimer}

  import swing.event.Key._

  val blockSize = 32
  val blockMargin = 1
  val offset = 6
  val H = Games.fieldSize._1 * (blockSize + blockMargin) + 2 * offset
  val V = Games.fieldSize._2 * (blockSize + blockMargin) + 2 * offset
  val mainPanelSize = new Dimension(H, V)
  val bluishGray = new AWTColor(48, 99, 99)
  val bluishLigtherGray = new AWTColor(79, 130, 130)
  val bluishEvenLigther = new AWTColor(145, 196, 196)
  val bluishDarker = new AWTColor(105, 156, 156)
  val bluishSilver = new AWTColor(210, 255, 255)
  val red = new AWTColor(217, 74, 105)

  implicit val system = ActorSystem("LocalSystem")
  implicit val timeout : Timeout = 10.seconds
  val config = ConfigFactory.load()
  val address = config.getString("user.server-address")
  var myUid : String = _ //= config.getString("user.my-uid")
  val remote = system.actorSelection("akka.tcp://server@" + address + "/user/Connector")

  var myConnection : ActorRef = _
  var myGameRoom : ActorRef = _
  var mySnake : ActorRef = _

  override def startup(args: Array[String]) = {
    myUid = args(0)
    println("Game uid is " + myUid)

    try {
      myConnection =
        Await.result(remote ? Messages.Connect(myUid), timeout.duration).asInstanceOf[Messages.Connected].connection
      println("Got connection ", myConnection)
      myGameRoom =
        Await.result(myConnection ? Messages.FindGame(myUid), timeout.duration).asInstanceOf[Messages.GameFound].gameInfo._2
      println("Got game ", myGameRoom)
      mySnake =
        Await.result(myGameRoom ? Messages.PlayerConnected(myUid), timeout.duration).asInstanceOf[Messages.SnakeCreated].snake
      println("Got snake ", mySnake)
    } catch {
      case _ : Throwable =>
        println("Initial connection failed")
        system.shutdown()
        quit()
    }

    super.startup(args)
  }

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
//        repaint()
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

    redrawTimer.start()
  }

  def onKeyPress(key: Key.Value) : Unit = key match {
    case Left  => mySnake ! Messages.Left
    case Right => mySnake ! Messages.Right
    case Up    => mySnake ! Messages.Up
    case Down  => mySnake ! Messages.Down
    case _ =>
  }

  def onPaint(g: Graphics2D) {
    val state = Await.result(myGameRoom ? Messages.GetState, timeout.duration).asInstanceOf[Messages.State]
    drawBoard(g, (offset, offset), (Games.fieldSize._1, Games.fieldSize._2), state.snake1, state.snake2, state.food)
  }

  def drawBoard(g: Graphics2D, offset: (Int, Int), gridSize: (Int, Int), snake1: List[(Int, Int)],
                snake2: List[(Int ,Int)], food: (Int, Int)) {
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
    def drawSnake1() = {
      g setColor bluishEvenLigther
      snake1 filter {_._2 < gridSize._2} foreach { b =>
        g fill buildRect(b) }
    }
    def drawSnake2() = {
      g setColor bluishDarker
      snake2 filter {_._2 < gridSize._2} foreach { b =>
        g fill buildRect(b) }
    }
    def drawCurrent() {
      g setColor bluishSilver
      g fill buildRect(food)
    }
    drawEmptyGrid()
    drawSnake1()
    drawSnake2()
    drawCurrent()
  }
}