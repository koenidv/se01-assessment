typealias board = Array<BooleanArray>

// Lights out game: https://daattali.com/shiny/lightsout/
fun main() {
    val content: board = Array(3) { BooleanArray(3) { true } }

    println("Welcome to Lights Out. Select a light using the respective number:")
    println("1 2 3\n4 5 6\n7 8 9\n")
    println("Let's go! Select a light.")

    content.print()

    var selected: Int
    while (!content.checkWon()) {
        println("\nWhich light would you like to toggle?")
        selected = handleInput()
        handleLightSelected(selected, content)
        if (content.checkWon()) {
            println("Great! You won!")
        }
    }

}

// Handle number inputs
fun handleInput(): Int {
    var input = 0
    var inputNumber: Int
    while (input == 0) {
        // Ask again whilst the input is not valid
        try {
            inputNumber = readln().toInt()

            // Only save the input if it is a number and between 1 and 9
            if (inputNumber in 1..9) {
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
fun handleLightSelected(selected: Int, content: board) {
    // Subtract 1 from selected because user counts from 1
    val selectAdjust = selected - 1
    // Calculate row and column from the numbers
    val row = (selectAdjust - selectAdjust % 3) / 3
    val column = selectAdjust % 3

    // Toggle the selected light
    content.toggle(row, column)

    // Toggle the adjacent lights if possible
    if (row > 0) content.toggle(row - 1, column)
    if (row < 2) content.toggle(row + 1, column)
    if (column > 0) content.toggle(row, column - 1)
    if (column < 2) content.toggle(row, column + 1)

    content.print()
}

// Prints the current game area
fun board.print() {
    for (row in this) {
        for (item in row) {
            // For each row and item, print the corresponding circle
            print(if (item) "⚫" else "⚪")
            print(" ")
        }
        print("\n") // New line for new row
    }
}

// Checks if the game is won, e.g. all lights are off
fun board.checkWon() = this.all { it.all { false } }

// Toggles an item by row and column
fun board.toggle(row: Int, column: Int) {
    this[row][column] = !this[row][column]
}
