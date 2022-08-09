package com.grapesapps.myapplication.view.screens.headphone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun HeadphoneModeControl(
    isConnected: Boolean,
    mainHeadsetValue: Int,
    onCheckedChange: (Int) -> Unit,
) {
    val statesHeadsetControl = remember {
        listOf("Шумоподавление", "Отключено", "Прозрачность")
    }
    val value = if (mainHeadsetValue == -1 || mainHeadsetValue > 2) 1 else mainHeadsetValue
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(15.dp))
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = MaterialTheme.colorScheme.onSecondary)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            statesHeadsetControl.forEachIndexed { index, text ->
                Text(
                    text = text,
                    color = if (text == statesHeadsetControl[value]) {
                        Color.Black
                    } else {
                        Color.White
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(15.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isConnected) {
                                onCheckedChange(index)
                            }
                        }
                        .background(
                            if (text == statesHeadsetControl[value]) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondary
                            }
                        )
                        .height(52.dp)
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .wrapContentHeight(Alignment.CenterVertically)
                        .padding(
                            vertical = 12.dp,
                            horizontal = 16.dp,
                        ),
                )
            }
        }
    }
}