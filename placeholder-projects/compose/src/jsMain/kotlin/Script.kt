import kotlinx.browser.*
import kotlinx.html.*
import kotlinx.html.dom.*
import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.checked
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.get
import kotlin.js.Date



data class Tube(
  var position: Int,
  val coordinates: List<Boolean>
)

data class GameFrame(
  val birdPos: Int,
  val tubes: List<Tube>,
  val isGameOver: Boolean,
  val isGameWon: Boolean,
  val score: Int,
)

interface Game {
  val gameFrame: State<GameFrame>
  fun step()
  fun moveBirdUp()
}

class ComposeBirdGame : Game {

  companion object {
    const val COLUMNS = 15
    const val ROWS = 9
    const val BIRD_COLUMN = 1
    private const val TUBES_START_FROM = (COLUMNS * 0.75).toInt()
    const val TOTAL_TUBES = 10
    private const val TUBE_HORIZONTAL_DISTANCE = 3
    private const val TUBE_VERTICAL_DISTANCE = 3
    private const val TUBE_WEIGHT = 500
    private const val BIRD_WEIGHT = 300
  }

  private val tubeGapRange = TUBE_VERTICAL_DISTANCE until ROWS
  private var tubeLastSteppedAt = 0.0
  private var birdLastSteppedAt = 0.0
  private var shouldMoveBirdUp = false

  private val _gameFrame: MutableState<GameFrame> by lazy {
    mutableStateOf(
      // First frame
      GameFrame(
        birdPos = ROWS / 2,
        tubes = buildLevel(),
        isGameOver = false,
        isGameWon = false,
        score = 0
      )
    )
  }

  /**
   * To build a random level
   */
  private fun buildLevel(): List<Tube> {
    return mutableListOf<Tube>().apply {
      var tubesAdded = 0
      var tubePosition = 0
      while (tubesAdded < TOTAL_TUBES) {
        if (tubePosition > TUBES_START_FROM && tubePosition % TUBE_HORIZONTAL_DISTANCE == 0) { // To give space to each tube
          add(
            Tube(
              tubePosition,
              buildRandomTube()
            )
          )
          tubesAdded++
        }
        tubePosition++
      }
    }
  }

  private fun buildRandomTube(): List<Boolean> {
    // creating a full tube
    val tube = mutableListOf<Boolean>().apply {
      repeat(ROWS) {
        add(true)
      }
    }

    // Adding gaps in random middle positions to make it two tubes.
    val gap1 = tubeGapRange.random()
    repeat(TUBE_VERTICAL_DISTANCE) { index ->
      tube[gap1 - index] = false
    }

    return tube
  }

  override val gameFrame: State<GameFrame> = _gameFrame

  override fun step() {
    update {
      val now = Date().getTime()

      // Stepping tube
      val tubeDiff = now - tubeLastSteppedAt
      val newTubes = if (tubeDiff > TUBE_WEIGHT) {
        tubeLastSteppedAt = now
        tubes.map {
          it.copy(position = it.position - 1)
        }
      } else {
        tubes
      }

      // Stepping bird position
      val birdDiff = now - birdLastSteppedAt
      val newBirdPos = when {
        shouldMoveBirdUp -> {
          birdLastSteppedAt = now
          shouldMoveBirdUp = false
          birdPos - 1 // move up
        }
        birdDiff > BIRD_WEIGHT -> {
          birdLastSteppedAt = now
          birdPos + 1 // move down
        }
        else -> {
          birdPos
        }
      }

      val newScore = newTubes.filter { it.position < BIRD_COLUMN }.size // All passed tube
      val newIsGameWon = newScore >= TOTAL_TUBES // If all tubes passed

      // Checking if bird gone out
      val newIsGameOver = if (newBirdPos < 0 || newBirdPos >= ROWS || isCollidedWithTube(newBirdPos, tubes)) {
        true
      } else {
        isGameOver
      }

      copy(
        isGameOver = newIsGameOver,
        tubes = newTubes,
        birdPos = newBirdPos,
        score = newScore,
        isGameWon = newIsGameWon
      )
    }
  }

  private fun isCollidedWithTube(newBirdPos: Int, tubes: List<Tube>): Boolean {
    val birdTube = tubes.find { it.position == BIRD_COLUMN }
    return birdTube?.coordinates?.get(newBirdPos) ?: false
  }

  override fun moveBirdUp() {
    shouldMoveBirdUp = true
  }

  private inline fun update(func: GameFrame.() -> GameFrame) {
    _gameFrame.value = _gameFrame.value.func()
  }
}

fun main() {

  val game: Game = ComposeBirdGame()

  val body = document.getElementsByTagName("body")[0] as HTMLElement

  body.addEventListener("keyup", {
    when ((it as KeyboardEvent).keyCode) {
      38 -> { // Arrow up
        game.moveBirdUp()
      }
    }
  })

  renderComposable(rootElementId = "root") {

    Div(
      attrs = {
        style {
          property("text-align", "center")
        }
      }
    ) {
      val gameFrame by game.gameFrame

      LaunchedEffect(Unit) {
        while (!gameFrame.isGameOver) {
          delay(60)
          game.step()
        }
      }

      Header(gameFrame)

      Div(
        attrs = {
          style {
            marginTop(30.px)
          }
        }
      ) {
        if (gameFrame.isGameOver || gameFrame.isGameWon) {
          GameResult(gameFrame)
        } else {
          // Play area
          repeat(ComposeBirdGame.ROWS) { rowIndex ->
            Div {
              repeat(ComposeBirdGame.COLUMNS) { columnIndex ->
                Input(
                  InputType.Radio,

                  attrs = {
                    val tube = gameFrame.tubes.find { it.position == columnIndex }
                    val isTube = tube?.coordinates?.get(rowIndex) ?: false
                    val isBird =
                      !isTube && columnIndex == ComposeBirdGame.BIRD_COLUMN && rowIndex == gameFrame.birdPos

                    if (isTube || isBird) {
                      checked()
                    }

                    if (!isBird) {
                      disabled()
                    }
                  }
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun Header(gameFrame: GameFrame) {
  H1 {
    Text(value = "🐦 Compose Bird!")
  }
  Text(value = "Your Score: ${gameFrame.score} || Top Score: ${ComposeBirdGame.TOTAL_TUBES}")
}

@Composable
private fun GameResult(gameFrame: GameFrame) {
  H2 {
    if (gameFrame.isGameWon) {
      Text("🚀 Won the game! 🚀")
    } else {
      Text("💀 Game Over 💀")
    }
  }
  Button(
    attrs = {
      onClick {
        window.location.reload()
      }
    }
  ) {
    Text("Try Again!")
  }
}
