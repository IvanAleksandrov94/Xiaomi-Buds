package com.grapesapps.myapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.widget.Toast
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.grapesapps.myapplication.entity.*
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.vm.Home
import com.grapesapps.myapplication.vm.HomeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@SuppressLint(
    "UnusedMaterial3ScaffoldPaddingParameter", "FrequentlyChangedStateReadInComposition", "NewApi",
    "MissingPermission"
)
@Composable
fun HeadsetScreen(
    navController: NavController?,
    viewModel: Home,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val state: State<HomeState?> = viewModel.state.observeAsState()
    val errorState: State<String?> = viewModel.errorState.observeAsState()
    val scrollState = rememberLazyListState()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberSplineBasedDecay(), rememberTopAppBarScrollState())

    var switchChecked by remember { mutableStateOf(true) }

    val lifecycleStateObserver = LocalLifecycleOwner.current.lifecycle.observeAsState()
    val lifecycleState = lifecycleStateObserver.value

    val bluetoothManager: BluetoothManager = remember { context.getSystemService(BluetoothManager::class.java) }
    val bluetoothAdapter: BluetoothAdapter? = remember { bluetoothManager.adapter }

    val btDevice = bluetoothAdapter?.bondedDevices?.firstOrNull { it.name == "Xiaomi Buds 3T Pro" }

    LaunchedEffect(key1 = viewModel, block = {
        launch {
            if (btDevice != null) {
                viewModel.connectDevice(btDevice)
            } else {
                viewModel.searchDevices()
            }
        }
    })


    LaunchedEffect(key1 = lifecycleState) {
        when (lifecycleState) {
            Lifecycle.Event.ON_RESUME -> {
                if (btDevice != null) {
                    viewModel.connectDevice(btDevice)
                }
            }
            Lifecycle.Event.ON_CREATE -> {
                print("!!")
            }
            Lifecycle.Event.ON_START -> {
                print("!!")

            }
            Lifecycle.Event.ON_PAUSE -> {
                print("!!")
            }
            else -> Unit
        }
    }

    when (errorState.value != null) {
        true -> Toast.makeText(context, errorState.value, Toast.LENGTH_SHORT).show()
        else -> Unit
    }


    when (val state = state.value) {
        is HomeState.HomeStateLoaded -> BudsApplicationTheme(
        ) {
            Scaffold(
                content = { innerPadding ->
                    LazyColumn(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        contentPadding = innerPadding,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = scrollState
                    ) {
                        item {
                            CenterAlignedTopAppBar(
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                                title = {
                                    Text("Xiaomi Buds 3T Pro")
                                },
                                scrollBehavior = scrollBehavior,
                            )
                        }
                        item {
                            HeadSetImage(height = configuration.screenHeightDp.dp / 3)
                        }
                        stickyHeader {
                            val firstItemVisible by remember {
                                derivedStateOf {
                                    scrollState.firstVisibleItemIndex >= 2
                                }
                            }
                            HeadSetInfo(
                                firstItemVisible,
                                state.isConnected,
                                l = state.leftHeadsetStatus,
                                r = state.rightHeadsetStatus,
                                c = state.caseHeadsetStatus,
                            )
                        }
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(Modifier.fillMaxSize()) {
                                    Text("Noise Control", Modifier.padding(15.dp), fontSize = 22.sp)
                                    HeadsetControl(
                                        isConnected = state.isConnected,
                                        mainHeadsetValue = state.mainHeadsetValue,
                                        onCheckedChange = {
                                            viewModel.changeMainSetting(it, state)
                                        }
                                    )
                                    when (state.headsetStatus?.setting) {
                                        HeadsetMainSetting.Noise -> {
                                            NoiseControl(
                                                value = state.headsetStatus.value,
                                                onCheckedChange = {
                                                    TODO("ВЫБОР ШУМОДАВА")
                                                    it
                                                    // viewModel.changeMainSetting(it, state)
                                                    //selectedNoise = it
                                                }
                                            )
                                        }

                                        HeadsetMainSetting.Transparency -> {
                                            Column() {
                                                switchChecked = state.headsetStatus.value == 1
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            start = 15.dp,
                                                            end = 15.dp,
                                                            top = 10.dp,
                                                            bottom = 10.dp
                                                        ),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Text("Услиление голоса", fontSize = 14.sp)
                                                    Switch(
                                                        modifier = Modifier.scale(0.8f),
                                                        checked = switchChecked,
                                                        colors = SwitchDefaults.colors(),
                                                        onCheckedChange = {
                                                          //  viewModel.changeMainSetting(it, state)
                                                            switchChecked = it
                                                        }

                                                    )
                                                }
                                                Divider(
                                                    color = MaterialTheme.colorScheme.onSecondary,
                                                    thickness = 1.5.dp
                                                )
                                            }
                                        }
                                        else -> Unit
                                    }

                                    Text(
                                        "Пространственное звучание",
                                        Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 5.dp),
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Отслеживание движения головы", fontSize = 14.sp)
                                        Switch(
                                            modifier = Modifier.scale(0.8f),
                                            checked = switchChecked,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = { switchChecked = it }

                                        )
                                    }
                                    Divider(
                                        Modifier.padding(top = 15.dp),
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        thickness = 1.5.dp
                                    )
                                    Text(
                                        "Гарнитура",
                                        Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 5.dp),
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp, vertical = 0.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Обнаружение уха", fontSize = 14.sp)
                                        Switch(
                                            modifier = Modifier.scale(0.8f),
                                            checked = switchChecked,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = { switchChecked = it }

                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp, vertical = 0.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Автоматический ответ", fontSize = 14.sp)
                                        Switch(
                                            modifier = Modifier.scale(0.8f),
                                            checked = switchChecked,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = { switchChecked = it }

                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp, vertical = 0.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Виджет в шторке уведомлений", fontSize = 14.sp)
                                        Switch(
                                            modifier = Modifier.scale(0.8f),
                                            checked = switchChecked,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = { switchChecked = it }

                                        )
                                    }
                                    Divider(
                                        Modifier.padding(top = 15.dp),
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        thickness = 1.5.dp
                                    )
                                    Text(
                                        "Свойства",
                                        Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 5.dp),
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp, vertical = 0.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Абсолютная громкость", fontSize = 14.sp)
                                        Switch(
                                            modifier = Modifier.scale(0.8f),
                                            checked = switchChecked,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = { switchChecked = it }

                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp, vertical = 0.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("LHDC", fontSize = 14.sp)
                                        Switch(
                                            modifier = Modifier.scale(0.8f),
                                            checked = switchChecked,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = { switchChecked = it }

                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp, vertical = 0.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Режим низкой задержки", fontSize = 14.sp)
                                        Switch(
                                            modifier = Modifier.scale(0.8f),
                                            checked = switchChecked,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = { switchChecked = it }

                                        )
                                    }
                                    Divider(
                                        Modifier.padding(top = 15.dp),
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        thickness = 1.5.dp
                                    )
                                    Row(
                                        modifier = Modifier
                                            .clickable { }
                                            .height(60.dp)
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp, vertical = 0.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Настройки", fontSize = 14.sp)
                                        Icon(Icons.Filled.ArrowForwardIos, "ArrowForwardIos", Modifier.scale(0.7f))

                                    }

                                    Row(
                                        modifier = Modifier
                                            .clickable { }
                                            .height(60.dp)
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp, vertical = 0.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Поиск наушников", fontSize = 14.sp)
                                        Icon(Icons.Filled.ArrowForwardIos, "ArrowForwardIos", Modifier.scale(0.7f))

                                    }

                                }
                            }
                        }
                        item {
                            TextButton(
                                modifier = Modifier.padding(start = 5.dp, end = 15.dp, top = 30.dp, bottom = 40.dp),
                              //  color = Color.Red,
                                onClick = {}){
                                Text("Удалить устройство", fontSize = 16.sp, color = Color.Red, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            )
        }
        else -> Box(modifier = Modifier) {
            CircularProgressIndicator()
        }
    }

}


@Composable
fun HeadSetImage(height: Dp) {
    val image = painterResource(R.drawable.headsetimage)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .height(height)
            .background(color = MaterialTheme.colorScheme.surface),
    ) {
        Image(
            painter = image,
            contentDescription = null,
            Modifier
                .fillMaxSize()
                .padding(horizontal = 50.dp, vertical = 25.dp),
            alignment = Alignment.TopCenter,
            contentScale = ContentScale.Fit
        )
    }
}


@Composable
fun HeadSetInfo(
    isVisibleImage: Boolean,
    isConnected: Boolean,
    l: LHeadsetBatteryStatus?,
    r: RHeadsetBatteryStatus?,
    c: CHeadsetBatteryStatus?,
) {
    val connectMessage = if (isConnected) "connected" else "disconnected"
    var left: String = ""
    var right: String = ""
    var case: String = ""
    if (l != null && l.battery != "-") {
        left = "L:${if (l.isCharging) "🔋" else ""}${l.battery}"
    }
    if (r != null && r.battery != "-") {
        right = "R:${if (r.isCharging) "🔋" else ""}${r.battery}"
    }
    if (c != null && c.battery != "-") {
        case = "C:${if (c.isCharging) "🔋" else ""}${c.battery}"
    }
    Box(
        modifier = Modifier
            .height(60.dp)
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp))
            .background(color = if (isVisibleImage) MaterialTheme.colorScheme.surface.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (isVisibleImage) 0.dp else 15.dp, vertical = 5.dp)
        ) {
            if (isVisibleImage) {
                val image = painterResource(R.drawable.headsetimage)
                Image(
                    painter = image,
                    contentDescription = null,
                    Modifier
                        .height(60.dp)
                        .width(60.dp)
                        .fillMaxSize()
                        .padding(start = 10.dp, top = 5.dp, bottom = 5.dp, end = 10.dp),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit
                )
            }

            Column() {
                Text(text = "Device $connectMessage", color = Color.White)
                Text(text = "$case $left $right", color = Color.White)
            }
        }
    }
}


@Composable
fun HeadsetControl(
    isConnected: Boolean,
    mainHeadsetValue: Int,
    onCheckedChange: (Int) -> Unit,
) {
    val statesHeadsetControl = listOf("Шумоподавление", "Отключено", "Прозрачность")
    val value = if (mainHeadsetValue == -1) 1 else mainHeadsetValue
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


@Composable
fun NoiseControl(
    onCheckedChange: (Int) -> Unit,
    value: Int,
) {
    Column() {
        val statesNoise = listOf("Адаптированное", "Слабое", "Сбалансированно", "Глубокое")
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

@Composable
fun Lifecycle.observeAsState(): State<Lifecycle.Event> {
    val state = remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
    DisposableEffect(this) {
        val observer = LifecycleEventObserver { _, event ->
            state.value = event
        }
        this@observeAsState.addObserver(observer)
        onDispose {
            this@observeAsState.removeObserver(observer)
        }
    }
    return state
}

@Preview
@Composable
fun HeadsetScreenPreview() {
    val viewModel = hiltViewModel<Home>()
    HeadsetScreen(null, viewModel = viewModel)
}