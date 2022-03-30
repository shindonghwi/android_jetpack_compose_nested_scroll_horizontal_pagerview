package com.example.jetpackcompose_netstedscroll_horizontalpagerview

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

@Composable
fun VerticalNestedScrollView(
    modifier: Modifier = Modifier,
    state: NestedScrollViewState,
    header: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
){
    NestedScrollView(
        modifier = modifier,
        state = state,
        header = header,
        content = content,
    )
}

@Composable
fun NestedScrollView(
    modifier: Modifier,
    state: NestedScrollViewState,
    header: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
){

    Layout(
        modifier = modifier
            .scrollable(
                orientation = Orientation.Vertical,
                state = rememberScrollableState {
                    state.drag(it)
                }
            )
            .nestedScroll(state.nestedScrollConnectionHolder),
        content = {
            Box {
                header.invoke()
            }

            Box{
               content.invoke()
            }
        }
    ){ measurables, constraints ->
        layout(constraints.maxWidth, constraints.maxHeight){
            val headerPlaceable = measurables[0].measure(constraints = constraints.copy(maxHeight = Constraints.Infinity))
            headerPlaceable.place(0, state.offset.roundToInt())
            state.updateBounds(maxOffset = -(headerPlaceable.height.toFloat()))
            val contentPlaceable = measurables[1].measure(constraints = constraints.copy(maxHeight = constraints.maxHeight))
            contentPlaceable.place(0, state.offset.roundToInt() + headerPlaceable.height)
        }
    }
}