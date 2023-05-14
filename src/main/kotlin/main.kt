import LIGHTSTATES.*

typealias Board = Array<Array<Light>> // 0: Off, 1: Red, 2: Green, 3: Blue

const val RESETCOLOR = "\u001b[0m"

enum class LIGHTSTATES(val displayColor: String, val symbol: String) {
    OFF(RESETCOLOR, "⚪"),
    WHITE(RESETCOLOR, "⚫"),
    RED("\u001b[31m", "⚫"),
    GREEN("\u001b[32m", "⚫"),
    BLUE("\u001b[34m", "⚫")
}

data class Position(val row: Int, val column: Int) {
    fun left() = Position(row, column - 1)
    fun right() = Position(row, column + 1)
    fun up() = Position(row - 1, column)
    fun down() = Position(row + 1, column)
}

// Lights out game: https://daattali.com/shiny/lightsout/
fun main() {
    LightsOffController()
}

class Light(var state: LIGHTSTATES, private val states: Array<LIGHTSTATES>) {
    fun toggle() {
        state = states[(states.indexOf(state) + 1) % states.size]
    }
}

abstract class BoardBaseModel(val size: Int) {
    val board = initBoard()
    abstract fun initBoard(): Board

    init {
        // For a random amount of moves between size^2 and size^2*10
        for (i in 0..(size * size..size * size * 10).random()) {
            // Do a random move within the board bounds
            handleLightSelectedInput(
                Position((0 until size).random(), (0 until size).random())
            )
        }
    }

    fun handleLightSelectedInput(position: Position) {
        // Toggle the selected light
        this.updateLight(position)

        // Toggle the adjacent lights if possible
        if (position.row > 0)
            this.updateLight(position.up())
        if (position.row < size - 1)
            this.updateLight(position.down())
        if (position.column > 0)
            this.updateLight(position.left())
        if (position.column < size - 1)
            this.updateLight(position.right())
    }

    private fun updateLight(position: Position) {
        board[position.row][position.column].toggle()
    }

    abstract fun checkWon(): Boolean
}

class BoardMonochrome(size: Int) : BoardBaseModel(size) {
    override fun initBoard() = Array(size) { Array(size) { Light(WHITE, arrayOf(OFF, WHITE)) } }

    override fun checkWon() = board.all { it.all { light -> light.state == OFF } }
}

class BoardColorful(size: Int) : BoardBaseModel(size) {
    private val states = arrayOf(WHITE, RED, GREEN, BLUE)
    override fun initBoard() = Array(size) { Array(size) { Light(BLUE, states) } }

    override fun checkWon() = board.all { it.all { light -> light.state == WHITE } }
}

open class View {
    fun getNumberInput(from: Int, to: Int): Int {
        var input = 0
        var inputNumber: Int

        while (input == 0) {
            // Ask again whilst the input is not valid
            try {
                inputNumber = readln().toInt()

                // Only save the input if it is a number and between 1 and 9
                if (inputNumber in from..to) {
                    input = inputNumber
                } else {
                    println("Please try again with a valid number.")
                }
            } catch (nfe: NumberFormatException) {
                println("Please try again with a valid number.")
            }
        }

        return input
    }
}

class ConfigView : View() {
    fun getColorInput(): Boolean {
        var input = ""
        while (input != "y" && input != "n") {
            println("Would you like to play with colors? (y/n)")
            input = readln()
        }
        return input == "y"
    }

    fun getSizeInput(): Int {
        println("\nWelcome to Lights Out. Please select a board size between 3 and 8.")
        return getNumberInput(from = 3, to = 8)
    }

    fun printInstructions() {
        println("Let's go! Select a light using the respective number.\n")
    }
}

class BoardView(private val model: BoardBaseModel) : View() {
    fun print() {
        model.board.forEach { row ->
            println(row.joinToString(separator = " ") {
                "${it.state.displayColor}${it.state.symbol}$RESETCOLOR"
            })
        }
    }

    fun printNumbers() {
        var counter = 1
        model.board.forEach { row ->
            println(row.joinToString(separator = " ") { counter++.toString() })
        }
        println()
    }

    fun printWon() {
        println("\nCongratulations! You won! You fixed all ${model.size * model.size} lights.")
    }

    fun getPositionInput(): Position {
        println("\nWhich light would you like to toggle?")
        return parseUserInputId(
            getNumberInput(from = 1, to = model.size * model.size)
        )
    }

    private fun parseUserInputId(id: Int): Position {
        val adjustedId = id - 1
        return Position(
            row = (adjustedId - adjustedId % model.size) / model.size,
            column = adjustedId % model.size
        )
    }
}

class LightsOffController {
    private var model: BoardBaseModel
    private var view: BoardView

    init {
        val config = ConfigView()
        model =
            if (config.getColorInput())
                BoardColorful(config.getSizeInput())
            else
                BoardMonochrome(config.getSizeInput())
        view = BoardView(model)

        config.printInstructions()
        view.printNumbers()

        runGame()
    }

    private fun runGame() {
        while (!model.checkWon()) {
            view.print()
            model.handleLightSelectedInput(view.getPositionInput())
        }
        view.print()
        view.printWon()
    }

}