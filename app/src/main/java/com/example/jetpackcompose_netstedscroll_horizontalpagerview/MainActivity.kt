package com.example.jetpackcompose_netstedscroll_horizontalpagerview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackcompose_netstedscroll_horizontalpagerview.ui.theme.JetpackCompose_NetstedScroll_HorizontalPagerViewTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetpackCompose_NetstedScroll_HorizontalPagerViewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = Color.Black
                ) {
                    NestedScrollview()
                }

            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun NestedScrollview() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Title: NestedScrollHorizontalView",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                },
                elevation = 0.dp
            )
        }
    ) {
        val scope = rememberCoroutineScope()
        val nestedScrollViewState = rememberNestedScrollViewState()
        VerticalNestedScrollView(
            state = nestedScrollViewState,
            header = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        IntroduceView(content = "Jetpack Compose", style = MaterialTheme.typography.h5)
                        Column(modifier = Modifier.padding(start = 20.dp)) {
                            IntroduceView(modifier = Modifier.padding(top = 10.dp), content = "- Call Me: 010-8940-6835")
                            IntroduceView(modifier = Modifier.padding(top = 5.dp), content = "- Email: shindonghwi8940@gmail.com")
                            IntroduceView(modifier = Modifier.padding(top = 5.dp), content = "- Name: Shin Dong Hwi")
                            IntroduceView(modifier = Modifier.padding(top = 5.dp), content = "- Developer: Wolf")
                            HyperLinkBlogText()
                        }


                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(text = "2021.04.06~:  BrandXFitness에서 일하는 중~", style = MaterialTheme.typography.caption, color = Color.Black)
                        }

                    }
                }
            },
            content = {
                val pagerState = rememberPagerState(initialPage = 0)
                val pages = (0 until 4).map { it }
                Column {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                            )
                        }
                    ) {
                        pages.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(text = "탭 ${title + 1}") },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                            )
                        }
                    }
                    HorizontalPager(
                        modifier = Modifier.weight(1f),
                        state = pagerState,
                        count = 4,
                    ) { page ->
                        LazyColumn {
                            items(150, key = { index -> index }) {
                                ListItem {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        elevation = 5.dp
                                    ) {
                                        Text(
                                            text = "${page + 1}페이지 ${it + 1}번 아이템",
                                            style = MaterialTheme.typography.h6,
                                            color = White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .background(Color.Companion.random())
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun IntroduceView(modifier: Modifier = Modifier, content: String, style: (androidx.compose.ui.text.TextStyle)? = null) {
    Text(
        modifier = modifier,
        text = content,
        style = style ?: MaterialTheme.typography.body1,
        color = Color.Black
    )
}


@Composable
private fun HyperLinkBlogText() {

    val activity = LocalContext.current as MainActivity

    val annotatedString = buildAnnotatedString {
        append("- Medium: ")

        pushStringAnnotation(tag = "blog", annotation = "https://medium.com/@wolf-android-developer")
        withStyle(style = SpanStyle(color = MaterialTheme.colors.primary, fontSize = 12.sp)) {
            append("https://medium.com/@wolf-android-developer")
        }
        pop()
    }

    ClickableText(
        modifier = Modifier.padding(top = 5.dp),
        text = annotatedString,
        style = MaterialTheme.typography.body1,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "blog", start = offset, end = offset).firstOrNull()?.let {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://medium.com/@wolf-android-developer")))
            }
        })

}


// 랜덤 칼라 가져오기
fun Color.Companion.random(): Color {
    val red = Random.nextInt(256)
    val green = Random.nextInt(256)
    val blue = Random.nextInt(256)
    return Color(red, green, blue)
}