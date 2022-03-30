package com.example.jetpackcompose_netstedscroll_horizontalpagerview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.withSign

/**
 * Android Develop [link]
 *  - [Saver]: https://developer.android.com/reference/kotlin/androidx/compose/runtime/saveable/Saver
 *
 * */


/**
 * NestedScrollView State Remember 생성
 *
 * - Saver를 통해, offset 초기 Value, Max Value를 설정한다.
 *
 * - Saver 구현을 하는 이유
 *     -> offset value를 저장 할 수 있는 값으로 변환하고, 가져오기 위함.
 *
 * */
@Composable
fun rememberNestedScrollViewState(): NestedScrollViewState {
    val scope = rememberCoroutineScope()
    val saver = remember { NestedScrollViewState.Saver(scope = scope) }

    return rememberSaveable(
        saver = saver
    ) {
        NestedScrollViewState(scope = scope)
    }
}

/** NestedScrollViewState 객체 생성,
 *
 * @param scope 코루틴 스코프 지정
 * @param initialOffset offset 초기값 설정
 * @param initialMaxOffset offset Max 값 설정
 *
 * */
@Stable
class NestedScrollViewState(
    private val scope: CoroutineScope,
    initialOffset: Float = 0f,
    initialMaxOffset: Float = 0f,
) {
    companion object {
        fun Saver(
            scope: CoroutineScope,
        ): Saver<NestedScrollViewState, *> = listSaver(
            save = { // offset을 저장한다.
                listOf(it.offset, it._maxOffset.value)
            },
            restore = { // 저장된 offset을 불러온다.
                NestedScrollViewState(
                    scope = scope,
                    initialOffset = it[0],
                    initialMaxOffset = it[1],
                )
            }
        )
    }

    private var changes = 0f

    private val _maxOffset = mutableStateOf(initialMaxOffset)
    val maxOffset: Float get() = _maxOffset.value.absoluteValue

    /** Animation이 가능한 Int value를 만든다.
     * Animation 중 값이 변경되면, 중단하고 새로운 값으로 전환되어, 값 변경이 계속 이루어진다.
     * */
    private var _offset = Animatable(initialOffset)
    val offset: Float get() = _offset.value

    internal val nestedScrollConnectionHolder = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            return takeIf {
                available.y < 0 && source == NestedScrollSource.Drag
            }?.let {
                Offset(0f, drag(available.y))
            } ?: Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return takeIf {
                available.y > 0 && source == NestedScrollSource.Drag
            }?.let {
                Offset(0f, drag(available.y))
            } ?: Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            return Velocity(0f, fling(available.y))
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            return Velocity(0f, fling(available.y))
        }
    }


    private suspend fun snapTo(value: Float) {
        _offset.snapTo(value)
    }

    internal suspend fun fling(velocity: Float): Float {
        if (velocity == 0f || velocity > 0 && offset == 0f) {
            return velocity
        }
        val realVelocity = velocity.withSign(changes)
        changes = 0f
        return if (offset > _maxOffset.value && offset <= 0) {
            _offset.animateDecay(
                realVelocity,
                exponentialDecay()
            ).endState.velocity.let {
                if (offset == 0f) {
                    velocity
                } else {
                    it
                }
            }
        } else {
            0f
        }
    }

    internal fun drag(delta: Float): Float {
        return if (delta < 0 && offset > _maxOffset.value || delta > 0 && offset < 0f) {
            changes = delta
            scope.launch {
                snapTo((offset + delta).coerceIn(_maxOffset.value, 0f))
            }
            delta
        } else {
            0f
        }
    }

    /**
     *
     * @Description offset의 범위를 업데이트 한다.
     *
     * */
    internal fun updateBounds(maxOffset: Float) {
        _maxOffset.value = maxOffset

        /**
         * updateBounds
         *  -> lowerBound가 upperBound 보다 크지 않은지 확인 후에 lowerBound, upperBound가 업데이트된다.
         *  -> 그리고 Animation이 Running중이 아닐떄, 값이 즉시 고정된다.
         *
         *  vertical scroll을 하면서, 상단으로 스크롤해서 header 뷰를 없애기 위함이라,
         *  lowerBound의 value offset은 0보다 작을 수 밖에없다.
         *
         *
         *  NestedScrollView에서 처음 Header View를 만들때, updateBounds를 호출하는데,
         *  이떄, Header View의 높이를 전달해준다.
         *
         *  따라서 offset의 범위는 [ 0 ~ Header View offset ] 이다.
         *
         *  min: 상단 컨텐츠가 전부 보이는 상태
         *  max: 상단 컨텐츠가 안보이는 상태
         *
         * */
        _offset.updateBounds(lowerBound = maxOffset, upperBound = 0f)
    }
}