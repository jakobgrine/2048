package com.example.a2048.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun NumberPicker(
    state: MutableState<Int>,
    modifier: Modifier = Modifier,
    range: IntRange? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    onStateChanged: (Int) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val numbersColumnHeight = 36.dp
    val halvedNumbersColumnHeight = numbersColumnHeight / 2
    val halvedNumbersColumnHeightPx =
        with(LocalDensity.current) { halvedNumbersColumnHeight.toPx() }

    fun animatedStateValue(offset: Float): Int =
        state.value - (offset / halvedNumbersColumnHeightPx).toInt()

    val animatedOffset = remember { Animatable(0f) }.apply {
        if (range != null) {
            val offsetRange = remember(state.value, range) {
                val value = state.value
                val first = -(range.last - value) * halvedNumbersColumnHeightPx
                val last = -(range.first - value) * halvedNumbersColumnHeightPx
                first..last
            }
            updateBounds(offsetRange.start, offsetRange.endInclusive)
        }
    }
    val coercedAnimatedOffset = animatedOffset.value % halvedNumbersColumnHeightPx
    val animatedStateValue = animatedStateValue(animatedOffset.value)

    Column(
        modifier = modifier
            .wrapContentSize()
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { deltaY ->
                    coroutineScope.launch {
                        animatedOffset.snapTo(animatedOffset.value + deltaY)
                    }
                },
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        val endValue = animatedOffset.fling(
                            initialVelocity = velocity,
                            animationSpec = exponentialDecay(frictionMultiplier = 20f),
                            adjustTarget = { target ->
                                val coercedTarget = target % halvedNumbersColumnHeightPx
                                val coercedAnchors = listOf(-halvedNumbersColumnHeightPx,
                                    0f,
                                    halvedNumbersColumnHeightPx)
                                val coercedPoint =
                                    coercedAnchors.minByOrNull { abs(it - coercedTarget) }!!
                                val base =
                                    halvedNumbersColumnHeightPx * (target / halvedNumbersColumnHeightPx).toInt()
                                coercedPoint + base
                            }
                        ).endState.value

                        state.value = animatedStateValue(endValue)
                        onStateChanged(state.value)
                        animatedOffset.snapTo(0f)
                    }
                }
            )
    ) {
        val spacing = 4.dp

        val arrowColor = MaterialTheme.colors.onSecondary.copy(alpha = ContentAlpha.disabled)

        // Arrow(direction = ArrowDirection.UP, tint = arrowColor)
        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Up")

        Spacer(modifier = Modifier.height(spacing))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
        ) {
            val baseLabelModifier = Modifier.align(Alignment.Center)
            ProvideTextStyle(textStyle) {
                Label(
                    text = (animatedStateValue - 1).toString(),
                    modifier = baseLabelModifier
                        .offset(y = -halvedNumbersColumnHeight)
                        .alpha(coercedAnimatedOffset / halvedNumbersColumnHeightPx)
                )
                Label(
                    text = animatedStateValue.toString(),
                    modifier = baseLabelModifier
                        .alpha(1 - abs(coercedAnimatedOffset) / halvedNumbersColumnHeightPx)
                )
                Label(
                    text = (animatedStateValue + 1).toString(),
                    modifier = baseLabelModifier
                        .offset(y = halvedNumbersColumnHeight)
                        .alpha(-coercedAnimatedOffset / halvedNumbersColumnHeightPx)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing))

        // Arrow(direction = ArrowDirection.DOWN, tint = arrowColor)
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Down")
    }
}

@Composable
private fun Label(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onLongPress = {
                // FIXME: Empty to disable text selection
            })
        }
    )
}

private suspend fun Animatable<Float, AnimationVector1D>.fling(
    initialVelocity: Float,
    animationSpec: DecayAnimationSpec<Float>,
    adjustTarget: ((Float) -> Float)?,
    block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
): AnimationResult<Float, AnimationVector1D> {
    val targetValue = animationSpec.calculateTargetValue(value, initialVelocity)
    val adjustedTarget = adjustTarget?.invoke(targetValue)

    return if (adjustedTarget != null) {
        animateTo(
            targetValue = adjustedTarget,
            initialVelocity = initialVelocity,
            block = block
        )
    } else {
        animateDecay(
            initialVelocity = initialVelocity,
            animationSpec = animationSpec,
            block = block,
        )
    }
}

// @Composable
// fun NumberPicker() {
//     AndroidView(factory = { context ->
//         android.widget.NumberPicker(context).apply {
//             minValue = 3
//             maxValue = 8
//             wrapSelectorWheel = false
//         }
//     })
//
//     // val size = 50.dp
//     // val min = 3
//     // val max = 8
//
//     // val listState = rememberLazyListState()
//     // val swipeableState = rememberSwipeableState(
//     //     3,
//     //     // confirmStateChange = {
//     //     //     scope.launch {
//     //     //         listState.scrollToItem(it)
//     //     //     }
//     //     //     true
//     //     // },
//     // )
//     // swipeableState.progress
//     // val anchors = with(LocalDensity.current) {
//     //     (min..max).associateBy { size.toPx() * it }
//     // }
//
//     // LaunchedEffect(swipeableState) {
//     //     snapshotFlow { swipeableState.offset }.collect {
//     //         listState.scrollBy(it.value)
//     //     }
//     // }
//     // Column(
//     //     modifier = Modifier
//     //         .height(size * 3)
//     //         .swipeable(
//     //             state = swipeableState,
//     //             orientation = Orientation.Vertical,
//     //             anchors = anchors,
//     //             thresholds = { _, _ -> FractionalThreshold(0.5f) },
//     //         ),
//     // ) {
//     //     repeat(max - min + 1) { index ->
//     //         Column(
//     //             modifier = Modifier.size(size),
//     //             verticalArrangement = Arrangement.Center,
//     //             horizontalAlignment = Alignment.CenterHorizontally,
//     //         ) {
//     //             Text(
//     //                 "${index + min}",
//     //                 modifier = Modifier.offset {
//     //                     IntOffset(0, swipeableState.offset.value.roundToInt())
//     //                 }
//     //             )
//     //         }
//     //     }
//     // }
//
//     // Box(
//     //     modifier = Modifier
//     //         .height(height)
//     //         .background(Color.DarkGray)
//     //         .swipeable(
//     //             state = state,
//     //             anchors = anchors,
//     //             orientation = Orientation.Vertical,
//     //             thresholds = { _, _ -> FractionalThreshold(0.3f) },
//     //         ),
//     // ) {
//     //     Box(
//     //         modifier = Modifier
//     //             .offset { IntOffset(0, state.offset.value.roundToInt()) }
//     //             .size(size)
//     //             .background(Color.Gray),
//     //     )
//     // }
// }