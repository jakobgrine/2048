package com.example.a2048.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.absoluteValue

@Preview
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    var ongoingGesture = false

    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Box {
                        TextButton(onClick = {
                            showMenu = true
                        }) {
                            Text("Board size")
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "Settings",
                                modifier = Modifier.size(30.dp),
                            )
                        }
                        DropdownMenu(expanded = showMenu,
                            onDismissRequest = { showMenu = false }) {
                            BOARD_SIZES.forEach {
                                DropdownMenuItem(onClick = {
                                    showMenu = false
                                    viewModel.boardSize = it
                                }) {
                                    Text("${it}x$it")
                                }
                            }
                        }
                    }
                    TextButton(onClick = viewModel::reset) {
                        Text("Reset")
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(all = 16.dp)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                if (ongoingGesture) {
                                    return@detectDragGestures
                                } else {
                                    ongoingGesture = true
                                }

                                val timeDelta =
                                    (change.uptimeMillis - change.previousUptimeMillis).toFloat()
                                val velocity = dragAmount / timeDelta

                                val direction =
                                    if (velocity.x.absoluteValue > velocity.y.absoluteValue) {
                                        if (velocity.x > 0) {
                                            Direction.Right
                                        } else {
                                            Direction.Left
                                        }
                                    } else {
                                        if (velocity.y > 0) {
                                            Direction.Down
                                        } else {
                                            Direction.Up
                                        }
                                    }
                                viewModel.action(direction)
                            },
                            onDragEnd = { ongoingGesture = false }
                        )
                    },
            ) {
                Text(if (viewModel.highscore > viewModel.score) "Highscore: ${viewModel.highscore}" else "")
                Text("Score: ${viewModel.score}")
                Spacer(modifier = Modifier.height(24.dp))
                Board(
                    board = viewModel.boardState,
                    boardSize = viewModel.boardSize,
                )
            }
        }
        AnimatedVisibility(
            visible = viewModel.gameOver,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                color = MaterialTheme.colors.background.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Game Over", color = MaterialTheme.colors.onBackground)
                    TextButton(onClick = {
                        viewModel.reset()
                    }) {
                        Text("Restart")
                    }
                }
            }
        }
    }
}