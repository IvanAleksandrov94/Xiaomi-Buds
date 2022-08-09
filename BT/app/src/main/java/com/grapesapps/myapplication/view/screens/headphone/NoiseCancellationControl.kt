package com.grapesapps.myapplication.view.screens.headphone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun NoiseCancellationControl(
    onCheckedChange: (Int) -> Unit,
    value: Int,
) {
    val statesNoise = remember {
        listOf("Адаптированное", "Слабое", "Сбалансированно", "Глубокое")
    }
    Column() {
        Text(
            "Уровень шумоподавления:",
            modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 20.dp),
            fontSize = 14.sp
        )
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.0f))
                .padding(start = 15.dp, end = 15.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            statesNoise.forEachIndexed { index, text ->
                Column(
                    Modifier
                        .height(IntrinsicSize.Min)
                        .width(IntrinsicSize.Min)
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                    ) {
                        Divider(
                            Modifier
                                .align(Alignment.Center)
                                .clip(
                                    shape = when (index) {
                                        0 -> {
                                            RoundedCornerShape(bottomStart = 10.dp, topStart = 10.dp)
                                        }
                                        3 -> {
                                            RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                                        }
                                        else -> {
                                            RoundedCornerShape(0.dp)
                                        }
                                    },
                                ),
                            thickness = 3.dp,
                            color = MaterialTheme.colorScheme.onSecondary,
                        )

                        if (text == statesNoise[value]) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        shape = RoundedCornerShape(15.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    .size(20.dp)
                                    .align(Alignment.Center)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onCheckedChange(index)
                                    }
                                    .height(IntrinsicSize.Min)
                                    .width(IntrinsicSize.Min)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(
                                        shape = RoundedCornerShape(15.dp),
                                        color =
                                        MaterialTheme.colorScheme.onSecondary,
                                    )
                                    .size(20.dp)
                                    .align(Alignment.Center)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onCheckedChange(index)
                                    }
                                    .height(IntrinsicSize.Min)
                                    .width(IntrinsicSize.Min)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            shape = RoundedCornerShape(15.dp),
                                            color =
                                            Color.Black
                                        )
                                        .size(10.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }

                    }
                    Box(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onCheckedChange(index)
                            }
                            .height(IntrinsicSize.Min)
                            .width(IntrinsicSize.Min)
                            .padding(horizontal = 15.dp)
                            .weight(1f)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .wrapContentHeight(Alignment.CenterVertically)

                    ) {

                        Text(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onCheckedChange(index)
                                }
                                .clip(shape = RoundedCornerShape(50.dp))
                                .height(52.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .wrapContentHeight(Alignment.CenterVertically)
                                .padding(vertical = 12.dp),
                            text = text,
                            color = if (text == statesNoise[value]) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                Color.White
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                    }
                }

            }
        }
        Divider(
            Modifier.padding(top = 5.dp),
            color = MaterialTheme.colorScheme.onSecondary,
            thickness = 1.5.dp
        )
    }

}


