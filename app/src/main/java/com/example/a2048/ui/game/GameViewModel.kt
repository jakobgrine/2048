package com.example.a2048.ui.game

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a2048.GameStateDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class Coordinate(var x: Int, var y: Int)

enum class Direction {
    Up, Right, Down, Left
}

typealias BoardState = SnapshotStateMap<Coordinate, Int>

val BOARD_SIZES = (3..5)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameStateDao: GameStateDao,
) : ViewModel() {
    var boardSize by mutableStateOf(4)
    var gameOver by mutableStateOf(false)

    private val boardStates = BOARD_SIZES.map { it to BoardState() }.toMutableStateMap()
    val boardState get() = boardStates[boardSize] ?: BoardState()

    private val scores = BOARD_SIZES.map { it to mutableStateOf(0) }.toMutableStateMap()
    var score
        get() = scores[boardSize]?.value ?: 0
        set(value) {
            val state = scores[boardSize]
            if (state != null)
                state.value = value
        }

    private val highscores = BOARD_SIZES.map { it to mutableStateOf(0) }.toMutableStateMap()
    var highscore
        get() = highscores[boardSize]?.value ?: 0
        set(value) {
            val state = highscores[boardSize]
            if (state != null)
                state.value = value
        }

    init {
        viewModelScope.launch {
            with(gameStateDao) {
                BOARD_SIZES.forEach { size ->
                    boardSize = size

                    // Load the board from local storage. If not found, start with a fresh board
                    boardFlow(size).first()?.let { boardState.putAll(it) } ?: reset()

                    // Load the (high)score from local storage
                    score = scoreFlow(size).first() ?: 0
                    highscore = highscoreFlow(size).first() ?: 0

                    // Check if the saved state was game over
                    gameOver = isGameOver()
                }
                boardSize = boardSizeFlow.first() ?: 4
            }
        }
    }

    fun reset() {
        score = 0
        gameOver = false
        boardState.clear()
        spawnRandomTile()
        spawnRandomTile()
        save()
    }

    fun action(direction: Direction) {
        when (direction) {
            Direction.Up -> up()
            Direction.Right -> right()
            Direction.Down -> down()
            Direction.Left -> left()
        }
    }

    private fun save() {
        viewModelScope.launch {
            gameStateDao.save(
                boardSize,
                boardStates,
                scores.mapValues { it.value.value },
                highscores.mapValues { it.value.value },
            )
        }
    }

    private fun left() {
        val scoreDelta = merge()
        val moved = move()
        score += scoreDelta
        if (scoreDelta > 0 || moved) {
            if (score > highscore) {
                highscore = score
            }

            spawnRandomTile()

            if (isGameOver()) {
                gameOver = true
            }

            save()
        }
    }

    private fun up() {
        transpose()
        left()
        transpose()
    }

    private fun right() {
        transpose()
        reflect()
        transpose()
        left()
        transpose()
        reflect()
        transpose()
    }

    private fun down() {
        reflect()
        transpose()
        left()
        transpose()
        reflect()
    }

    private fun isGameOver(): Boolean {
        if (getFreeFields().isNotEmpty()) {
            return false
        }
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val here = boardState[Coordinate(col, row)]
                val right = boardState[Coordinate(col + 1, row)]
                val bottom = boardState[Coordinate(col, row + 1)]

                if (here == right) return false
                if (here == bottom) return false
            }
        }
        return true
    }

    private fun getFreeFields(): Set<Coordinate> = HashSet<Coordinate>().apply {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val coordinate = Coordinate(col, row)
                if (coordinate !in boardState) {
                    add(coordinate)
                }
            }
        }
    }

    /**
     * Inserts a new tile in a random free field.
     * The tile is 2 with a 90% chance and 4 with a 10% chance.
     */
    private fun spawnRandomTile() {
        // Get a random free field
        val free = getFreeFields().random()

        boardState[free] = if (Random.nextInt(10) == 0) 4 else 2
    }

    /**
     * Applies a transform to the field coordinates.
     */
    private fun transform(func: Coordinate.() -> Unit) {
        val new = HashMap<Coordinate, Int>()
        for ((k, v) in boardState) {
            if (k == Coordinate(-1, -1))
                continue

            new[k.apply(func)] = v
        }
        new[Coordinate(-1, -1)] = boardSize
        boardState.clear()
        boardState.putAll(new)
    }

    /**
     * Reflects the board on its main diagonal.
     */
    private fun transpose() = transform {
        x = y.also { y = x }
    }

    /**
     * Reflects the board on its horizontal center line.
     */
    private fun reflect() = transform {
        y = boardSize - y - 1
    }

    /**
     * Merges cells with the same value (even with space between them).
     *
     * @return the accumulated score of all merged values.
     */
    private fun merge(): Int {
        var scoreDelta = 0
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                // Look for a non-empty field
                val left = Coordinate(col, row)
                if (left !in boardState) {
                    continue
                }

                for (i in col + 1 until boardSize) {
                    // Look for a field with the same value
                    val right = Coordinate(i, row)
                    if (right !in boardState) {
                        continue
                    }

                    if (boardState[left] == boardState[right]) {
                        // Merge the two values
                        boardState[left] = boardState[left]!! * 2
                        scoreDelta += (boardState.remove(right) ?: 0) * 2
                    }
                    break
                }
            }
        }
        return scoreDelta
    }

    /**
     * Moves cells to the left.
     *
     * @return `true` if cells were modified, else `false`.
     */
    private fun move(): Boolean {
        var changed = false
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                // Look for an empty field
                val left = Coordinate(col, row)
                if (left in boardState) {
                    continue
                }

                for (i in col + 1 until boardSize) {
                    // Look for a non-empty field
                    val right = Coordinate(i, row)
                    if (right !in boardState) {
                        continue
                    }

                    // Move the non-empty field to the left
                    boardState[left] = boardState[right]!!
                    boardState.remove(right)
                    changed = true
                    break
                }
            }
        }
        return changed
    }
}