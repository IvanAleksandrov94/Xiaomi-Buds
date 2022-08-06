package com.grapesapps.myapplication


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*

@Composable
fun MainApp(
    events: List<Event>,
    isScreenRound: Boolean,
    onQueryOtherDevicesClicked: () -> Unit,
    onQueryMobileCameraClicked: () -> Unit
) {
    //  val scalingLazyListState = rememberScalingLazyListState()
    var selectedNoiseSetting by remember { mutableStateOf(1) }
//    var checked2 by remember { mutableStateOf(true) }
//    var checked3 by remember { mutableStateOf(false) }
    var checkedChip by remember { mutableStateOf(false) }
    var selectedNoise by remember { mutableStateOf(1) }
    Log.e("SCREEN", "isScreenRound: $isScreenRound")

    Scaffold(

        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = { TimeText() }
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp, start = 5.dp, end = 5.dp),
            Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Xiaomi Buds 3T Pro".map { it }.joinToString(
                        separator = "",
                        limit = 18,
                        truncated = "...",
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Clip,

                    )
                Text(
                    text = "C:100% L: 100% R: 100%",
                    fontSize = 8.sp

                )
                Row() {
                    Text(
                        text = "● ",
                        fontSize = 8.sp,
                        color = Color.Green
                    )
                    Text(
                        text = "Подключено",
                        fontSize = 8.sp

                    )


                }
                Box(modifier = Modifier.height(8.dp))


            }
            Box(
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
            ) {
                when (selectedNoise) {
                    0 -> NoiseControl(
                        value = 1,
                        onCheckedChange = { }
                    )
                    1 -> Text(
                        "Режимы отключены", fontSize = 8.sp,
                        modifier = Modifier.padding(
                            bottom = 8.dp
                        ),
                    )
                    2 -> {
                        ToggleChip(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .padding(bottom = 8.dp, start = 10.dp, end = 10.dp),
                            checked = checkedChip,
                            toggleControl = {
                                Icon(
                                    imageVector = ToggleChipDefaults.switchIcon(checked = checkedChip),
                                    contentDescription = if (checkedChip) "Checked" else "Unchecked"
                                )

                            },
                            enabled = true,
                            onCheckedChange = {
                                checkedChip = it
                            },
                            colors = ToggleChipDefaults.toggleChipColors(
                                uncheckedStartBackgroundColor = Color(0xFF273141),
                                checkedStartBackgroundColor = Color(0xFF273141),
                                checkedToggleControlColor = Color(0xFFd8e3f8)
                            ),
                            label = {
                                Text("Усиление голоса", fontSize = 8.sp)
                            },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.BottomCenter)
                    .height(IntrinsicSize.Min)
                    .width(IntrinsicSize.Min)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    ToggleButton(
                        modifier = Modifier
                            .padding(bottom = 25.dp, end = 5.dp)
                            .size(40.dp),
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            uncheckedBackgroundColor = Color(0xFF273141)
                        ),
                        checked = selectedNoise == 0,
                        onCheckedChange = {
                            if (selectedNoise != 0) {
                                selectedNoise = 0
                            }
                        },
                        enabled = true,
                    ) {
                        Icon(
                            painter = painterResource(id = R.mipmap.noise_control_on),
                            contentDescription = "Noise on",
                            modifier = Modifier
                                .size(20.dp)
                                .wrapContentSize(align = Alignment.Center),
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    ToggleButton(
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                            .size(45.dp),
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            uncheckedBackgroundColor = Color(0xFF273141)
                        ),
                        checked = selectedNoise == 1,
                        onCheckedChange = {
                            if (selectedNoise != 1) {
                                selectedNoise = 1
                            }

                        },
                        enabled = true,
                    ) {
                        Icon(
                            painter = painterResource(id = R.mipmap.noise_control_off),
                            contentDescription = "Noise off",
                            modifier = Modifier
                                .size(ToggleButtonDefaults.DefaultIconSize)
                                .wrapContentSize(align = Alignment.Center),
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom

                ) {
                    ToggleButton(
                        modifier = Modifier
                            .padding(bottom = 25.dp, start = 5.dp)
                            .size(40.dp),
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            uncheckedBackgroundColor = Color(0xFF273141)
                        ),
                        checked = selectedNoise == 2,
                        onCheckedChange = {
                            if (selectedNoise != 2) {
                                selectedNoise = 2
                            }

                        },
                        enabled = true,
                    ) {
                        Icon(
                            painter = painterResource(id = R.mipmap.noise_aware),
                            contentDescription = "Transparency",
                            modifier = Modifier
                                .size(20.dp)
                                .wrapContentSize(align = Alignment.Center),
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun NoiseControl(
    onCheckedChange: (Int) -> Unit,
    value: Int,
) {
    Column() {
        val statesNoise = listOf("Адап.", "Слаб.", "Сбал.", "Глуб.")
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = MaterialTheme.colors.primary.copy(alpha = 0.0f))
                .padding(start = 5.dp, end = 5.dp)
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
                        Box(
                            Modifier
                                .height(2.dp)
                                .fillMaxWidth()
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
                                )
                                .background(Color(0xFF273141))
                        )
                        if (text == statesNoise[value]) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        shape = RoundedCornerShape(15.dp),
                                        color = MaterialTheme.colors.primary
                                    )
                                    .size(15.dp)
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
                                        Color(0xFF273141)
                                    )
                                    .size(15.dp)
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
                            .padding(horizontal = 0.dp)
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
                                .height(25.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .wrapContentHeight(Alignment.CenterVertically)
                                .padding(vertical = 1.dp),
                            text = text,
                            color = if (text == statesNoise[value]) {
                                MaterialTheme.colors.primary
                            } else {
                                Color.White
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            fontSize = 8.sp,
                            lineHeight = 10.sp,
                        )
                    }
                }

            }
        }
    }

}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun MainAppPreviewEvents() {
    MainApp(
        events = listOf(
            Event(
                title = R.string.data_item_changed,
                text = "Event 1"
            ),
            Event(
                title = R.string.data_item_deleted,
                text = "Event 2"
            ),
            Event(
                title = R.string.data_item_unknown,
                text = "Event 3"
            ),
            Event(
                title = R.string.message,
                text = "Event 4"
            ),
            Event(
                title = R.string.data_item_changed,
                text = "Event 5"
            ),
            Event(
                title = R.string.data_item_deleted,
                text = "Event 6"
            )
        ),
        isScreenRound = false,
        onQueryOtherDevicesClicked = {},
        onQueryMobileCameraClicked = {}
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun MainAppPreviewEmpty() {
    MainApp(
        isScreenRound = false,
        events = emptyList(),
        onQueryOtherDevicesClicked = {},
        onQueryMobileCameraClicked = {}
    )
}
