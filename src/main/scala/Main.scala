import scala.swing._
import scala.swing.event._

object Main extends SimpleSwingApplication {
  import java.awt.{Color => AWTColor}
  import javax.swing.{AbstractAction, Timer => SwingTimer}

  import event.Key._

  val mainPanelSize = new Dimension(544, 544)
  val bluishGray = new AWTColor(48, 99, 99)
  val bluishLigtherGray = new AWTColor(79, 130, 130)
  val bluishEvenLigther = new AWTColor(145, 196, 196)
  val bluishSilver = new AWTColor(210, 255, 255)
  val red = new AWTColor(217, 74, 105)
  val blockSize = 32
  val blockMargin = 1
  var isEnded = false
  val state = new GameState

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

    val redrawTimer = new SwingTimer(100, new AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) {
        repaint()
      }
    })

    val snakeMovementTimer = new SwingTimer(300, new AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) {
        moveSnake()
      }
    })

    redrawTimer.start()
    snakeMovementTimer.start()

    def moveSnake() : Unit = {
      if (state.move()) {
        redrawTimer.stop()
        snakeMovementTimer.stop()
        println("Stop")
        isEnded = true
        repaint()
      }
    }
  }

  def onKeyPress(key: Key.Value) : Unit = key match {
    case Left  => state.receive(GameState.Left)
    case Right => state.receive(GameState.Right)
    case Up    => state.receive(GameState.Up)
    case Down  => state.receive(GameState.Down)
    case _ =>
  }

  def onPaint(g: Graphics2D) {
    val curState = state.getGameState
    drawBoard(g, (6, 6), (16, 16), curState.blocks, curState.food)
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
    if (isEnded) {
      g setColor red
      g fill buildRect(blocks.head)
    }
  }
}
