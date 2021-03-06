typealias board = Array<IntArray> // 0: Off, 1: Red, 2: Green, 3: Blue

const val RED = "\u001b[31m"
const val GREEN = "\u001b[32m"
const val BLUE = "\u001b[34m"
const val RESETCOLOR = "\u001b[0m"

// Lights out game: https://daattali.com/shiny/lightsout/
fun main() {
    println("\nWelcome to Lights Out. Please select a board size between 3 and 8.")
    val size = handleInput(from = 3, to = 8)
    println("selected $size")
    val content = generateBoardEasy(size)

    // Usage instructions
    println("Select a light using the respective number:")
    content.printNumbers()
    println("Let's go! Select a light.")
    content.print()

    var selected: Int
    while (!content.checkWon()) {
        // Ask for a light selection and toggle the lights until the game is won
        println("\nWhich light would you like to toggle?")
        selected = handleInput(to = size * size)
        content.handleLightSelected(selected, size)

        // Print the updated board
        content.print()

        // Congratulate if game is won
        if (content.checkWon()) {
            println("Great! You won!")
        }
    }
}

// Generate a random board, the easy way
// Do this by creating an empty board, then doing random moves on it.
fun generateBoardEasy(size: Int): board {
    val content: board = Array(size) { IntArray(size) { 1 } }

    // For a random amount of moves between size^2 and size^2*10
    for (i in 0..(size * size..size * size * 10).random()) {
        // Do a random move within the board bounds
        content.handleLightSelected((1..size * size).random(), size)
    }

    return content
}

// Handle number inputs
fun handleInput(from: Int = 1, to: Int = 9): Int {
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
            println("Please try again with a valid number. (NFE)")
        }
    }
    return input
}

// Handle toggling the selected and adjacent lights
fun board.handleLightSelected(selected: Int, size: Int = 3) {
    // Subtract 1 from selected because user counts from 1
    val selectAdjust = selected - 1
    // Calculate row and column from the numbers
    val row = (selectAdjust - selectAdjust % size) / size
    val column = selectAdjust % size

    // Toggle the selected light
    this.toggle(row, column)

    // Toggle the adjacent lights if possible
    if (row > 0) this.toggle(row - 1, column)
    if (row < size - 1) this.toggle(row + 1, column)
    if (column > 0) this.toggle(row, column - 1)
    if (column < size - 1) this.toggle(row, column + 1)
}

// Prints the current game area
fun board.print() {
    for (row in this) {
        for (item in row) {
            // For each row and item, print the corresponding circle
            print(
                when (item) {
                    1 -> "$RED???$RESETCOLOR"
                    2 -> "$GREEN???$RESETCOLOR"
                    3 -> "$BLUE???$RESETCOLOR"
                    else -> "???"
                }
            )
            print(" ")
        }
        print("\n") // New line for new row
    }
}

// Prints a number for each item
fun board.printNumbers() {
    var counter = 1
    for (row in this) {
        for (item in row) {
            print(counter)
            print(" ")
            counter++
        }
        print("\n")
    }
}

// Checks if the game is won, e.g. all lights are off
fun board.checkWon() = this.all { it.all { false } }

// Increases an item at a row and column by 1, or resets to 0
// if end of range is reached
fun board.toggle(row: Int, column: Int) {
    this[row][column] += 1
    if (this[row][column] > 3) this[row][column] = 0
}
