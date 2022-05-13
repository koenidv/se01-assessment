typealias board = Array<IntArray> // 0: Off, 1: Red, 2: Green, 3: Blue

const val RED = "\u001b[31m"
const val GREEN = "\u001b[32m"
const val BLUE = "\u001b[34m"
const val RESETCOLOR = "\u001b[0m"

// Lights out game: https://daattali.com/shiny/lightsout/
fun main() {
    println("\nWelcome to Lights Out. Please select a board size between 3 and 8.")
    val size = handleInput(from = 3, to = 8)

    println("Would you like to play with different colors? y/n")
    val colors = handleBooleanInput()

    val content = if (colors || size != 5) generateBoardEasy(size, colors) else generateBoardMonochrome(size)

    // Usage instructions
    println("Nice. Select a light by typing the respective number:")
    content.printNumbers()
    println("Press enter to continue")
    readln()
    println("Let's go! Select a light.")
    content.print(colors = colors)

    var selected: Int
    while (!content.checkWon()) {
        // Ask for a light selection and toggle the lights until the game is won
        println("\nWhich light would you like to toggle?")
        selected = handleInput(to = size * size)
        content.handleLightSelected(selected, colors)

        // Print the updated board
        content.print(colors = colors)

        // Congratulate if game is won
        if (content.checkWon()) {
            println("Great! You won!")
        }
    }
}

// Generate a random board, the easy way
// Do this by creating an empty board, then doing random moves on it.
fun generateBoardEasy(size: Int, colors: Boolean = true): board {
    val content: board = Array(size) { IntArray(size) { 1 } }

    // For a random amount of moves between size^2 and size^2*10
    for (i in 0..(size * size..size * size * 10).random()) {
        // Do a random move within the board bounds
        content.handleLightSelected((1..size * size).random())
    }

    return content
}

// More complex method for generating a 5x5 board for on/off lights
fun generateBoardMonochrome(size: Int): board {
    var content: board = Array(size) { IntArray(size) { (0..1).random() } }

    while (!content.testSolvable5by5Monochrome()) {
        content = Array(size) { IntArray(size) { (0..1).random() } }
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

// Handle boolean inputs
fun handleBooleanInput(): Boolean {
    var input: Boolean? = null
    var inputString = ""
    while (input == null) {
        inputString = readln()
        if (inputString == "y" || inputString == "yes") input = true
        else if (inputString == "n" || inputString == "no") input = false
        else println("Please use y or n")
    }
    return input
}

// Handle toggling the selected and adjacent lights
fun board.handleLightSelected(selected: Int, colors: Boolean = true) {
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
fun board.print(colors: Boolean = true) {
    for (row in this) {
        for (item in row) {
            // For each row and item, print the corresponding circle
            if (colors) {
                print(
                    when (item) {
                        1 -> "$RED⚫$RESETCOLOR"
                        2 -> "$GREEN⚫$RESETCOLOR"
                        3 -> "$BLUE⚫$RESETCOLOR"
                        else -> "⚪"
                    }
                )
            } else {
                print(if (item == 1) "⚫" else "⚪")
            }
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
fun board.checkWon() = this.all { it.all { it == 0 } }

// Increases an item at a row and column by 1, or resets to 0
// if end of range is reached
fun board.toggle(row: Int, column: Int, colors: Boolean = true) {
    if (colors) {
        this[row][column] += 1
        if (this[row][column] > 3) this[row][column] = 0
    } else {
        this[row][column] = if (this[row][column] == 1) 0 else 1
    }
}

// Gets the item at a position from 1 until size^2
fun board.getItem(selection: Int): Int {
    val row = ((selection - 1) - (selection - 1) % size) / size
    val column = (selection - 1) % size
    return this[row][column]
}

// Check if a on/off board is solvable
// Based on this: http://faculty.cooper.edu/smyth/cs111/lightout/lo_alg1.htm
fun board.testSolvable5by5Monochrome(): Boolean {
    // Copy the board to work on it
    val copy = this.copyOf()
    var row: Int
    var column: Int

    // Step 1, toggle items below checked items
    val step1 = {
        for (i in (1 until size * size - size)) {
            row = ((i - 1) - (i - 1) % size) / size
            column = (i - 1) % size

            if (copy[row][column] == 1) copy.toggle(row + 1, column)
        }
    }
    step1()

    // Step 2
    if (copy[4][0] != copy[4][4] || copy[4][1] != copy[4][3]) {
        copy.toggle(0, 0)
        copy.toggle(1, 1)
        copy.toggle(1, 2)
        copy.toggle(1, 3)
        copy.toggle(2, 2)
        copy.toggle(2, 4)
        copy.toggle(3, 3)
        copy.toggle(4, 4)
        copy.toggle(4, 4)
        step1()
    }

    // Step 3
    val step3 = {
        for (i in indices) {
            if (copy[size - 1][i] == 1 && i != size - 1) {
                copy.toggle(size - 1, i + 1)
                break
            }
        }
    }
    step3()
    step3()
    step3()

    // Step 4
    for (i in size * size - size - 1 downTo size + 1) {
        row = ((i - 1) - (i - 1) % size) / size
        column = (i - 1) % size

        if (copy[row][column] == 1) copy.toggle(row - 1, column)
    }

    val solvable = copy.checkWon()
    println(solvable)
    return solvable

}