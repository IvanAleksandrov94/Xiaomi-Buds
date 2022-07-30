@file:OptIn(ExperimentalComposeUiApi::class)

package com.grapesapps.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme as m3
import androidx.compose.material3.Switch
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.grapesapps.myapplication.model.SharedPrefManager
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.view.navigation.Navigation
import com.grapesapps.myapplication.vm.Home
import com.grapesapps.myapplication.vm.HomeState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.util.*
//
//@AndroidEntryPoint
//@OptIn(ExperimentalAnimationApi::class)
//class MainActivity : ComponentActivity() {
//    private lateinit var navController: NavHostController
//    private lateinit var pref: SharedPrefManager
//
//    @OptIn(ExperimentalUnsignedTypes::class, ExperimentalComposeUiApi::class)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            pref = SharedPrefManager(LocalContext.current)
//            navController = rememberAnimatedNavController()
//
//            Navigation(
//                navController = navController,
//            )
//        }
//    }
//}

@Composable
fun HeadSetImage(height: Dp) {
    val image = painterResource(R.drawable.headsetimage)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .height(height)
            .background(color = m3.colorScheme.surface),
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
fun HeadSetInfo(isVisibleImage: Boolean) {
    Box(
        modifier = Modifier
            .height(60.dp)
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp))
            .background(color = if (isVisibleImage) m3.colorScheme.surface.copy(alpha = 0.9f) else m3.colorScheme.surface)
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
                Text(text = "Device connected", color = Color.White)
                Text(text = "C: 100% L: 100% R: 100%", color = Color.White)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "FrequentlyChangedStateReadInComposition")
@Composable
fun HomeScreen(
    navController: NavController?,
    viewModel: Home,
) {
//
    val configuration = LocalConfiguration.current
    // val state: State<HomeState?> = viewModel.viewState.observeAsState()
    val scrollState = rememberLazyListState()
//    val barState = rememberTopAppBarScrollState()
//    val dc = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarScrollState())
    val statesHeadsetControl = remember { mutableStateListOf("Шумоподавление", "Отключено", "Прозрачность") }
    val statesShum = remember { mutableStateListOf("Адаптированное", "Слабое", "Сбалансированно", "Глубокое") }
    var selectedHeadsetControl by remember {
        mutableStateOf(statesHeadsetControl[0])
    }

    var switchChecked by remember { mutableStateOf(true) }

    var selectedshum by remember {
        mutableStateOf(statesShum[0])
    }

    LaunchedEffect(key1 = viewModel, block = {
        launch {
            viewModel.loadInfo()
        }
    })

    BudsApplicationTheme(
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
                                containerColor = m3.colorScheme.surface,
                                //scrolledContainerColor = m3.colorScheme.secondaryContainer,
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
                        HeadSetInfo(firstItemVisible)
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxSize()) {
                                Text("Noise Control", Modifier.padding(15.dp), fontSize = 22.sp)
                                HeadsetControl(
                                    selected = selectedHeadsetControl,
                                    states = statesHeadsetControl,
                                    onCheckedChange = {
                                        selectedHeadsetControl = it
                                        it
                                    }
                                )
                                when (selectedHeadsetControl) {
                                    statesHeadsetControl[0] -> {

                                        ShumControl(
                                            selected = selectedshum,
                                            states = statesShum,
                                            onCheckedChange = {
                                                selectedshum = it
                                                it
                                            }
                                        )
                                    }
                                    statesHeadsetControl[1] -> {}
                                    statesHeadsetControl[2] -> {
                                        Column() {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text("Услиление голоса", fontSize = 14.sp)
                                                Switch(
                                                    modifier = Modifier.scale(0.8f),
                                                    checked = switchChecked,
                                                    colors = SwitchDefaults.colors(

                                                    ),
                                                    onCheckedChange = { switchChecked = it }

                                                )
                                            }
                                            Divider(
                                                color = m3.colorScheme.onSecondary,
                                                thickness = 1.5.dp
                                            )
                                        }
                                    }
                                }

                                Text(
                                    "Пространственное звучание",
                                    Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 5.dp),
                                    fontSize = 15.sp,
                                    color = m3.colorScheme.primary
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
                                    Modifier.padding( top = 15.dp),
                                    color = m3.colorScheme.onSecondary,
                                    thickness = 1.5.dp
                                )
                                Text(
                                    "Гарнитура",
                                    Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 5.dp),
                                    fontSize = 15.sp,
                                    color = m3.colorScheme.primary
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
                                    color = m3.colorScheme.onSecondary,
                                    thickness = 1.5.dp
                                )
                                Text(
                                    "Свойства",
                                    Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 5.dp),
                                    fontSize = 15.sp,
                                    color = m3.colorScheme.primary
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
                                    Modifier.padding( top = 15.dp),
                                    color = m3.colorScheme.onSecondary,
                                    thickness = 1.5.dp
                                )
                                Row(
                                    modifier = Modifier.clickable {  }
                                        .height(60.dp)
                                        .fillMaxWidth()
                                        .padding(horizontal = 15.dp, vertical = 0.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Настройки", fontSize = 14.sp)
                                    Icon(Icons.Filled.ArrowForwardIos, "ArrowForwardIos",Modifier.scale(0.7f))

                                }

                                Row(
                                    modifier = Modifier.clickable {  }
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
                        Text(
                            "Удалить устройство",
                            Modifier.padding(start = 15.dp, end = 15.dp, top = 30.dp, bottom = 40.dp),
                            fontSize = 16.sp,
                            color = Color.Red
                        )
                    }

                }
            }
        )
    }
}


@Composable
fun HeadsetControl(selected: String, states: List<String>, onCheckedChange: (String) -> String) {
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(15.dp))
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = m3.colorScheme.onSecondary)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            states.forEach { text ->
                Text(
                    text = text,
                    color = if (text == selected) {
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
                        .clickable {
                            onCheckedChange(text)
                        }
                        .background(
                            if (text == selected) {
                                m3.colorScheme.onSecondaryContainer
                            } else {
                                m3.colorScheme.onSecondary
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
fun ShumControl(selected: String, states: List<String>, onCheckedChange: (String) -> Any?) {
    Column() {
        Text(
            "Уровень шумоподавления:",
            modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 20.dp),
            fontSize = 14.sp
        )
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = m3.colorScheme.onSecondary.copy(alpha = 0.0f))
                .padding(start = 15.dp, end = 15.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            states.forEachIndexed { i, text ->
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
                                    shape = when (i) {
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
                            color = m3.colorScheme.onSecondary,
                        )

                        if (text == selected) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        shape = RoundedCornerShape(15.dp),
                                        color = m3.colorScheme.onSecondaryContainer,
                                    )
                                    .size(20.dp)
                                    .align(Alignment.Center)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onCheckedChange(text)
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
                                        m3.colorScheme.onSecondary,
                                    )
                                    .size(20.dp)
                                    .align(Alignment.Center)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onCheckedChange(text)
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
                                onCheckedChange(text)
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
                                    onCheckedChange(text)
                                }
                                .clip(shape = RoundedCornerShape(50.dp))
                                .height(52.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .wrapContentHeight(Alignment.CenterVertically)
                                .padding(vertical = 12.dp),
                            text = text,
                            color = if (text == selected) {
                                m3.colorScheme.onSecondaryContainer
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
            color = m3.colorScheme.onSecondary,
            thickness = 1.5.dp
        )
    }

}

@Preview
@Composable
fun Preview() {
    val viewModel = hiltViewModel<Home>()
    HomeScreen(null, viewModel = viewModel)
}


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "BT_CONNECT"

        // 13 byte headset info and version
        private val headsetInfo = listOf(
            0xFE, 0xDC, 0xBA, 0xC1, 0x02, 0x00, 0x05,
            0x02, 0xFF, 0xFF, 0xFF, 0xFF, 0xEF
        )

        // 14 byte Disable mode
        private val arr0 = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x07,
            0x02, 0x04, 0x00, 0xef
        )

        // 12 byte ШУМ
        private val arr1 = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x05,
            0x02, 0x04, 0x01, 0xef
        )

        // 12 byte Прозрачность
        private val arr2 = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x06,
            0x02, 0x04, 0x02, 0xef
        )

        private val checkHeadsetMode = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x07,
            0x02, 0x04, 0x05, 0xef
        )

        private lateinit var bluetoothManager: BluetoothManager
        private lateinit var headphones: BluetoothDevice
        private val uuid: UUID = UUID.fromString("0000fd2d-0000-1000-8000-00805f9b34fb")
        private var myDevice: BluetoothDevice? = null
        private fun byteArrayOfInts(ints: List<Int>) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

    }

    private val btClassicReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            Log.e(TAG, "${intent.action}")

            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, "Device discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Device discovery finished")
                    myDevice?.fetchUuidsWithSdp()

                }
                BluetoothDevice.ACTION_UUID -> {
                    val uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                    Log.d(TAG, "${uuidExtra?.toSet()}")
                }

                BluetoothDevice.ACTION_FOUND -> {
                    Log.d(TAG, "ACTION_FOUND")
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    Log.d(TAG, "${device?.name}")
                    Log.d(TAG, "${device?.address}")
                    if (device?.name == "Xiaomi Buds 3T Pro") {
                        myDevice = device
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        CoroutineScope(Dispatchers.IO).launch {
            val isConnected = BluetoothService.connected()
            val isConnection = BluetoothService.statusConnection
            if (isConnected && !isConnection) {
                BluetoothService.sendData(byteArrayOfInts(headsetInfo))
                BluetoothService.sendData(byteArrayOfInts(checkHeadsetMode))
            } else if (!isConnected && !isConnection) {
                BluetoothService.connectDevice(headphones, uuid)
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        val isConnected = BluetoothService.connected()
        if (!isConnected) {
            CoroutineScope(Dispatchers.IO).launch {
                BluetoothService.connectDevice(headphones, uuid)
            }
        }

    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothManager.adapter.isDiscovering) {
            bluetoothManager.adapter.cancelDiscovery()
        }
        if (BluetoothService.connected()) {
            BluetoothService.disconnect()
        }
    }

    override fun onStop() {
        super.onStop()
//        if (BluetoothService.connected()) {
//            BluetoothService.disconnect()
//        }
    }


    //@OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission", "UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.WAKE_LOCK,
                ),
                1
            )
        }

        bluetoothManager = applicationContext
            .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        try {
            val device = bluetoothManager
                .adapter
                .bondedDevices
                .firstOrNull { it.name == "Xiaomi Buds 3T Pro" }

            if (device != null) {
                headphones = device
            }

//            CoroutineScope(Dispatchers.IO).launch {
//                BluetoothService.connectDevice(headphones, uuid)
//            }

//            val intentFilter = IntentFilter().apply {
//                addAction(BluetoothDevice.ACTION_FOUND)
//                addAction(BluetoothDevice.ACTION_UUID)
//                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
//                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//            }
//            if (bluetoothManager.adapter.isDiscovering) {
//                bluetoothManager.adapter.cancelDiscovery();
//            }
//            registerReceiver(btClassicReceiver, intentFilter)


        } catch (e: NullPointerException) {
            Log.e(TAG, e.message ?: "NullPointerException")

        }
        fun UUID.asBytes(): ByteArray {
            val b = ByteBuffer.wrap(ByteArray(16))
            b.putLong(mostSignificantBits)
            b.putLong(leastSignificantBits)
            return b.array()
        }
        setContent {
         //   MyApplicationTheme {
                Scaffold(
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Button(onClick = {


                            CoroutineScope(Dispatchers.IO).launch {
                                BluetoothService.connectDevice(headphones, uuid)
//                                val isConnected = BluetoothService.connected()
//                                val isConnection = BluetoothService.statusConnection
//                                if (isConnected && !isConnection) {
//                                    BluetoothService.sendData(byteArrayOfInts(headsetInfo))
//                                    BluetoothService.sendData(byteArrayOfInts(checkHeadsetMode))
//                                } else if (!isConnected && !isConnection) {
//                                    BluetoothService.connectDevice(headphones, uuid)
//                                }
                            }

                        }) {
                            Text("TEST")

                        }
                        Button(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                when (BluetoothService.connected()) {
                                    true -> BluetoothService.sendData(byteArrayOfInts(headsetInfo))
                                    false -> BluetoothService.connectDevice(headphones, uuid)
                                }
                            }
                        }) {
                            Text("Device INFO")
                        }

                        Button(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    when (BluetoothService.connected()) {
                                        true -> BluetoothService.sendData(byteArrayOfInts(arr1))
                                        false -> BluetoothService.connectDevice(headphones, uuid)
                                    }
                                }

                            }

                        ) {
                            Text("Шум")
                        }

                        Button(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                when (BluetoothService.connected()) {
                                    true -> BluetoothService.sendData(byteArrayOfInts(arr2))
                                    false -> BluetoothService.connectDevice(headphones, uuid)
                                }
                            }


                        }) {
                            Text("Прозрачность")
                        }
                        Button(onClick = {


                            CoroutineScope(Dispatchers.IO).launch {
                                when (BluetoothService.connected()) {
                                    true -> BluetoothService.sendData(byteArrayOfInts(arr0))
                                    false -> BluetoothService.connectDevice(headphones, uuid)
                                }
                            }

                        }) {
                            Text("Отключить")
                        }
                    }
                }
          //  }
        }
    }
}


