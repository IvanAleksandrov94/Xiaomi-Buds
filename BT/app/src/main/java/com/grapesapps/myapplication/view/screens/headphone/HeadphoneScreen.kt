package com.grapesapps.myapplication.view.screens.headphone

import android.content.Context
import android.content.Intent
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.grapesapps.myapplication.view.navigation.Screen
import com.grapesapps.myapplication.vm.HeadphoneState
import com.grapesapps.myapplication.vm.HeadphoneVm
import dev.olshevski.navigation.reimagined.NavController
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import com.grapesapps.myapplication.entity.HeadsetMainSetting
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.view.observeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HeadphoneScreen(
    navController: NavController<Screen>,
    viewModel: HeadphoneVm,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val state: State<HeadphoneState?> = viewModel.viewStateHeadphone.observeAsState()
    val lifecycleStateObserver = LocalLifecycleOwner.current.lifecycle.observeAsState()
    val lifecycleState = lifecycleStateObserver.value
    val scrollState = rememberLazyListState()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberSplineBasedDecay(), rememberTopAppBarState())
    var switchChecked by remember { mutableStateOf(false) }

    fun bindBluetoothService() {
        val connection = viewModel.getServiceConnection()
        Intent(
            context,
            BluetoothSDKService::class.java
        ).also { intent ->
            context.bindService(
                intent,
                connection,
                Context.BIND_AUTO_CREATE,
            )
        }
    }

    LaunchedEffect(
        key1 = viewModel,
        block = {
            launch {
                bindBluetoothService()
                viewModel
            }
        }
    )

    BudsApplicationTheme {
        Scaffold(
            content = { contentPadding ->
                Box(
                  //  modifier = Modifier.padding(contentPadding)
                ) {
                    LazyColumn(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        contentPadding = contentPadding,
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
//                            HeadSetInfo(
//                                firstItemVisible,
//                                state.isConnected,
//                                l = state.leftHeadsetStatus,
//                                r = state.rightHeadsetStatus,
//                                c = state.caseHeadsetStatus,
//                            )
                        }
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(Modifier.fillMaxSize()) {
                                    Text("Noise Control", Modifier.padding(15.dp), fontSize = 22.sp)
//                                    HeadphoneModeControl(
//                                        isConnected = state.isConnected,
//                                        mainHeadsetValue = state.mainHeadsetValue,
//                                        onCheckedChange = {
//                                            viewModel.changeMainSetting(it, state)
//                                        }
//                                    )
//                                    when (state.headsetStatus?.setting) {
//                                        HeadsetMainSetting.Noise -> {
//                                            NoiseCancellationControl(
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
                                            onCheckedChange = {
                                                if (it) {
                                                    GlobalScope.launch(Dispatchers.IO) {

//                                                        val result = btHeadset.sendVendorSpecificResultCode(
//                                                            btDevice,
//                                                            "+XIAOMI",
//                                                            "FF01020103020501FF"
//                                                        )
//                                                        Log.e("SelectSpectral", "Send result $result")

                                                    }


                                                } else {
//                                                    val result = btHeadset.sendVendorSpecificResultCode(
//                                                        btDevice,
//                                                        "+XIAOMI",
//                                                        "FF01020103020500FF"
//                                                    )
//
//                                                    Log.e("SelectSpectral", "Send result $result")
                                                }
                                                //switchChecked = it
                                                //  viewModel.onSelectSpectralAudio()
                                                // switchChecked = it
                                            }

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
                                            checked = false,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = {
                                              //  viewModel.onSelectAutoSearchEar()
                                                //  switchChecked = it
                                            }

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
                                            checked = false,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = {
                                              //  viewModel.onSelectAutoPhoneAnswer()
                                                //   switchChecked = it
                                            }

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
                                            checked = false,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = {

                                            }

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
                                            checked = false,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = {
                                                //   switchChecked = it
                                            }

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
                                            checked = false,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = {
                                                //   switchChecked = it
                                            }

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
                                            checked = false,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = {
                                                //switchChecked = it
                                            }

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
                        item {
                            TextButton(
                                modifier = Modifier.padding(start = 5.dp, end = 15.dp, top = 30.dp, bottom = 40.dp),
                                //  color = Color.Red,
                                onClick = {}) {
                                Text(
                                    "Удалить устройство",
                                    fontSize = 16.sp,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        )
    }

}