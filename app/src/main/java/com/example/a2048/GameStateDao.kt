package com.example.a2048

import android.content.Context
import androidx.compose.runtime.toMutableStateMap
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.a2048.ui.game.BoardState
import com.example.a2048.ui.game.Coordinate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameStateDao @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.dataStore

    private val BOARD_SIZE = intPreferencesKey("board_size")
    private fun BOARD(size: Int) = stringSetPreferencesKey("board_state_$size")
    private fun SCORE(size: Int) = intPreferencesKey("score_$size")
    private fun HIGHSCORE(size: Int) = intPreferencesKey("highscore_$size")

    val boardSizeFlow: Flow<Int?> = dataStore.data.map { it[BOARD_SIZE] }
    fun boardFlow(size: Int): Flow<BoardState?> = dataStore.data.map {
        it[BOARD(size)]?.map { value ->
            val vs = value.split(",").map { it.toInt() }
            Coordinate(vs[0], vs[1]) to vs[2]
        }?.toMutableStateMap()
    }

    fun scoreFlow(size: Int): Flow<Int?> = dataStore.data.map { it[SCORE(size)] }
    fun highscoreFlow(size: Int): Flow<Int?> = dataStore.data.map { it[HIGHSCORE(size)] }

    suspend fun save(
        boardSize: Int,
        boardStates: Map<Int, Map<Coordinate, Int>>,
        scores: Map<Int, Int>,
        highscores: Map<Int, Int>,
    ) = dataStore.edit { settings ->
        settings[BOARD_SIZE] = boardSize
        boardStates.forEach { (size, state) ->
            settings[BOARD(size)] = state
                .map { (coordinate, value) -> "${coordinate.x},${coordinate.y},$value" }
                .toSet()
        }
        scores.forEach { (size, score) ->
            settings[SCORE(size)] = score
        }
        highscores.forEach { (size, highscore) ->
            settings[HIGHSCORE(size)] = highscore
        }
    }
}