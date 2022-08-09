package com.grapesapps.myapplication

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.grapesapps.myapplication.entity.CHeadsetBatteryStatus
import com.grapesapps.myapplication.entity.LHeadsetBatteryStatus
import com.grapesapps.myapplication.entity.RHeadsetBatteryStatus
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.view.navigation.Screen
import dev.olshevski.navigation.reimagined.NavController
import kotlinx.coroutines.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@SuppressLint(
    "UnusedMaterial3ScaffoldPaddingParameter", "FrequentlyChangedStateReadInComposition", "NewApi",
    "MissingPermission"
)
@Composable
fun HeadsetTScreen(
    navController: NavController<Screen>,
   // viewModel: Home,
) {
    val context = LocalContext.current
//    val configuration = LocalConfiguration.current
//    val state: State<HomeState?> = viewModel.state.observeAsState()
//    val errorState: State<String?> = viewModel.errorState.observeAsState()
//    val scrollState = rememberLazyListState()
//    val scrollBehavior =
//        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberSplineBasedDecay(), rememberTopAppBarState())
//
//    var switchChecked by remember { mutableStateOf(false) }
//
//    val lifecycleStateObserver = LocalLifecycleOwner.current.lifecycle.observeAsState()
//    val lifecycleState = lifecycleStateObserver.value
//
//    val bluetoothManager: BluetoothManager = remember { context.getSystemService(BluetoothManager::class.java) }
//    val bluetoothAdapter: BluetoothAdapter? = remember { bluetoothManager.adapter }
//
//    val btDevice = bluetoothAdapter?.bondedDevices?.firstOrNull { it.name == "Xiaomi Buds 3T Pro" }
//
//    val mMediaPlayer = MediaPlayer.create(context, R.raw.xiaomi_sound)
//
//    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//
//    lateinit var btHeadset: BluetoothHeadset
//
//    val isActiveMusic = audioManager.isMusicActive
//    Log.e("MAIN", "is active Music $isActiveMusic")


//
//    bluetoothManager.adapter?.getProfileProxy(
//        context, object : BluetoothProfile.ServiceListener {
//            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
//                if (profile == BluetoothProfile.HEADSET) {
//                    val mCurrentHeadset = proxy as BluetoothHeadset
//                    btHeadset = mCurrentHeadset
//                    Log.i("MAIN", "BluetoothHeadset ПОДКЛЮЧЕН")
////                    val connect = proxy.javaClass.getDeclaredMethod(
////                        "connect",
////                        BluetoothDevice::class.java
////                    )
////                    connect.isAccessible = true
////                    connect.invoke(proxy, btDevice)
//                    //  BluetoothAdapter.getDefaultAdapter().closeProfileProxy(profile, proxy)
//                }
//
//
//            }
//
//
//            override fun onServiceDisconnected(profile: Int) {
//                if (profile == BluetoothProfile.HEADSET) {
//                    //btHeadset = null
//                    Log.i("MAIN", "BluetoothHeadset ОТКЛЮЧЕН")
//                }
//            }
//
//        }, BluetoothProfile.HEADSET
//    )



//    LaunchedEffect(key1 = viewModel, block = {
//        launch {
////            if (btDevice != null) {
////                viewModel.connectDevice(btDevice, audio = audioManager)
////            } else {
////                viewModel.searchDevices()
////            }
//        }
//    })

//
//
//
//
//
//    LaunchedEffect(key1 = lifecycleState) {
//        when (lifecycleState) {
//            Lifecycle.Event.ON_RESUME -> {
//                if (btDevice != null) {
//                    viewModel.connectDevice(btDevice, audioManager)
//                }
//            }
//            Lifecycle.Event.ON_CREATE -> {
//                print("!!")
//            }
//            Lifecycle.Event.ON_START -> {
//                print("!!")
//
//            }
//            Lifecycle.Event.ON_PAUSE -> {
//                print("!!")
//            }
//            else -> Unit
//        }
//    }
//
//    when (errorState.value != null) {
//        true -> Toast.makeText(context, errorState.value, Toast.LENGTH_SHORT).show()
//        else -> Unit
//    }
    BudsApplicationTheme {
        Scaffold(
            content = { contentPadding ->
                Box(
                    modifier = Modifier.padding(contentPadding)
                ) {

                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Content of the page",
                            fontSize = 30.sp,
                        )
                    }
                }
            }
        )
    }

//    when (val state = state.value) {
//        is HomeState.HomeStateLoaded -> BudsApplicationTheme(
//        ) {
//            Scaffold(
//                content = { innerPadding ->
//                    LazyColumn(
//                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
//                        contentPadding = innerPadding,
//                        verticalArrangement = Arrangement.spacedBy(8.dp),
//                        state = scrollState
//                    ) {
//                        item {
//                            CenterAlignedTopAppBar(
//                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                                    containerColor = MaterialTheme.colorScheme.surface,
//                                ),
//                                title = {
//                                    Text("Xiaomi Buds 3T Pro")
//                                },
//                                scrollBehavior = scrollBehavior,
//                            )
//                        }
//                        item {
//                            HeadSetImage(height = configuration.screenHeightDp.dp / 3)
//                        }
//                        stickyHeader {
//                            val firstItemVisible by remember {
//                                derivedStateOf {
//                                    scrollState.firstVisibleItemIndex >= 2
//                                }
//                            }
//                            HeadSetInfo(
//                                firstItemVisible,
//                                state.isConnected,
//                                l = state.leftHeadsetStatus,
//                                r = state.rightHeadsetStatus,
//                                c = state.caseHeadsetStatus,
//                            )
//                        }
//                        item {
//                            Card(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                            ) {
//                                Column(Modifier.fillMaxSize()) {
//                                    Text("Noise Control", Modifier.padding(15.dp), fontSize = 22.sp)
//                                    HeadsetControl(
//                                        isConnected = state.isConnected,
//                                        mainHeadsetValue = state.mainHeadsetValue,
//                                        onCheckedChange = {
//                                            viewModel.changeMainSetting(it, state)
//                                        }
//                                    )
//                                    when (state.headsetStatus?.setting) {
//                                        HeadsetMainSetting.Noise -> {
//                                            NoiseControl(
//                                                value = state.headsetStatus.value,
//                                                onCheckedChange = {
//                                                    TODO("ВЫБОР ШУМОДАВА")
//                                                    it
//                                                    // viewModel.changeMainSetting(it, state)
//                                                    //selectedNoise = it
//                                                }
//                                            )
//                                        }
//
//                                        HeadsetMainSetting.Transparency -> {
//                                            Column() {
//                                                //  switchChecked = state.headsetStatus.value == 1
//                                                Row(
//                                                    modifier = Modifier
//                                                        .fillMaxWidth()
//                                                        .padding(
//                                                            start = 15.dp,
//                                                            end = 15.dp,
//                                                            top = 10.dp,
//                                                            bottom = 10.dp
//                                                        ),
//                                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                                    verticalAlignment = Alignment.CenterVertically,
//                                                ) {
//                                                    Text("Услиление голоса", fontSize = 14.sp)
//                                                    Switch(
//                                                        modifier = Modifier.scale(0.8f),
//                                                        checked = false,
//                                                        colors = SwitchDefaults.colors(),
//                                                        onCheckedChange = {
//                                                            //  viewModel.changeMainSetting(it, state)
//                                                            // switchChecked = it
//                                                        }
//
//                                                    )
//                                                }
//                                                Divider(
//                                                    color = MaterialTheme.colorScheme.onSecondary,
//                                                    thickness = 1.5.dp
//                                                )
//                                            }
//                                        }
//                                        else -> Unit
//                                    }
//
//                                    Text(
//                                        "Пространственное звучание",
//                                        Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 5.dp),
//                                        fontSize = 15.sp,
//                                        color = MaterialTheme.colorScheme.primary
//                                    )
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 15.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically,
//                                    ) {
//                                        Text("Отслеживание движения головы", fontSize = 14.sp)
//                                        Switch(
//                                            modifier = Modifier.scale(0.8f),
//                                            checked = switchChecked,
//                                            colors = SwitchDefaults.colors(
//
//                                            ),
//                                            onCheckedChange = {
//                                                if (it) {
//                                                    GlobalScope.launch(Dispatchers.IO) {
//
//                                                        val result = btHeadset.sendVendorSpecificResultCode(
//                                                            btDevice,
//                                                            "+XIAOMI",
//                                                            "FF01020103020501FF"
//                                                        )
//                                                        Log.e("SelectSpectral", "Send result $result")
//
//                                                    }
//
//
//                                                } else {
//                                                    val result = btHeadset.sendVendorSpecificResultCode(
//                                                        btDevice,
//                                                        "+XIAOMI",
//                                                        "FF01020103020500FF"
//                                                    )
//
//                                                    Log.e("SelectSpectral", "Send result $result")
//                                                }
//                                                switchChecked = it
//                                                //  viewModel.onSelectSpectralAudio()
//                                                // switchChecked = it
//                                            }
//
//                                        )
//                                    }
//                                    Divider(
//                                        Modifier.padding(top = 15.dp),
//                                        color = MaterialTheme.colorScheme.onSecondary,
//                                        thickness = 1.5.dp
//                                    )
//                                    Text(
//                                        "Гарнитура",
//                                        Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 5.dp),
//                                        fontSize = 15.sp,
//                                        color = MaterialTheme.colorScheme.primary
//                                    )
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 15.dp, vertical = 0.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically,
//                                    ) {
//                                        Text("Обнаружение уха", fontSize = 14.sp)
//                                        Switch(
//                                            modifier = Modifier.scale(0.8f),
//                                            checked = false,
//                                            colors = SwitchDefaults.colors(
//
//                                            ),
//                                            onCheckedChange = {
//                                                viewModel.onSelectAutoSearchEar()
//                                                //  switchChecked = it
//                                            }
//
//                                        )
//                                    }
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 15.dp, vertical = 0.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically,
//                                    ) {
//                                        Text("Автоматический ответ", fontSize = 14.sp)
//                                        Switch(
//                                            modifier = Modifier.scale(0.8f),
//                                            checked = false,
//                                            colors = SwitchDefaults.colors(
//
//                                            ),
//                                            onCheckedChange = {
//                                                viewModel.onSelectAutoPhoneAnswer()
//                                                //   switchChecked = it
//                                            }
//
//                                        )
//                                    }
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 15.dp, vertical = 0.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically,
//                                    ) {
//                                        Text("Виджет в шторке уведомлений", fontSize = 14.sp)
//                                        Switch(
//                                            modifier = Modifier.scale(0.8f),
//                                            checked = false,
//                                            colors = SwitchDefaults.colors(
//
//                                            ),
//                                            onCheckedChange = {
//                                                viewModel.onSelectSpectralAudio()
//                                                if (mMediaPlayer.isPlaying) {
//                                                    mMediaPlayer.stop()
//                                                    mMediaPlayer.prepareAsync();
//                                                } else {
//                                                    viewModel.onStartHeadTest()
//                                                    mMediaPlayer.isLooping = false
//                                                    mMediaPlayer.start()
//                                                }
//                                                viewModel.onStartHeadTest()
//                                                switchChecked = it
//                                            }
//
//                                        )
//                                    }
//                                    Divider(
//                                        Modifier.padding(top = 15.dp),
//                                        color = MaterialTheme.colorScheme.onSecondary,
//                                        thickness = 1.5.dp
//                                    )
//                                    Text(
//                                        "Свойства",
//                                        Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 5.dp),
//                                        fontSize = 15.sp,
//                                        color = MaterialTheme.colorScheme.primary
//                                    )
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 15.dp, vertical = 0.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically,
//                                    ) {
//                                        Text("Абсолютная громкость", fontSize = 14.sp)
//                                        Switch(
//                                            modifier = Modifier.scale(0.8f),
//                                            checked = false,
//                                            colors = SwitchDefaults.colors(
//
//                                            ),
//                                            onCheckedChange = {
//                                                //   switchChecked = it
//                                            }
//
//                                        )
//                                    }
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 15.dp, vertical = 0.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically,
//                                    ) {
//                                        Text("LHDC", fontSize = 14.sp)
//                                        Switch(
//                                            modifier = Modifier.scale(0.8f),
//                                            checked = false,
//                                            colors = SwitchDefaults.colors(
//
//                                            ),
//                                            onCheckedChange = {
//                                                //   switchChecked = it
//                                            }
//
//                                        )
//                                    }
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 15.dp, vertical = 0.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically,
//                                    ) {
//                                        Text("Режим низкой задержки", fontSize = 14.sp)
//                                        Switch(
//                                            modifier = Modifier.scale(0.8f),
//                                            checked = false,
//                                            colors = SwitchDefaults.colors(
//
//                                            ),
//                                            onCheckedChange = {
//                                                //switchChecked = it
//                                            }
//
//                                        )
//                                    }
//                                    Divider(
//                                        Modifier.padding(top = 15.dp),
//                                        color = MaterialTheme.colorScheme.onSecondary,
//                                        thickness = 1.5.dp
//                                    )
//                                    Row(
//                                        modifier = Modifier
//                                            .clickable { }
//                                            .height(60.dp)
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 15.dp, vertical = 0.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically,
//                                    ) {
//                                        Text("Настройки", fontSize = 14.sp)
//                                        Icon(Icons.Filled.ArrowForwardIos, "ArrowForwardIos", Modifier.scale(0.7f))
//
//                                    }
//
//                                    Row(
//                                        modifier = Modifier
//                                            .clickable { }
//                                            .height(60.dp)
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 15.dp, vertical = 0.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically,
//                                    ) {
//                                        Text("Поиск наушников", fontSize = 14.sp)
//                                        Icon(Icons.Filled.ArrowForwardIos, "ArrowForwardIos", Modifier.scale(0.7f))
//
//                                    }
//
//                                }
//                            }
//                        }
//                        if (state.fwInfo != null) {
//                            item {
//                                Text(
//                                    "Версия прошивки ${state.fwInfo.version}",
//                                    modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 5.dp),
//                                    fontSize = 12.sp,
//                                    textAlign = TextAlign.Center
//                                )
//                            }
//                        }
//                        item {
//                            TextButton(
//                                modifier = Modifier.padding(start = 5.dp, end = 15.dp, top = 30.dp, bottom = 40.dp),
//                                //  color = Color.Red,
//                                onClick = {}) {
//                                Text(
//                                    "Удалить устройство",
//                                    fontSize = 16.sp,
//                                    color = Color.Red,
//                                    textAlign = TextAlign.Center
//                                )
//                            }
//                        }
//                    }
//                }
//            )
//        }
//        else -> Box(modifier = Modifier) { }
 //   }

}





