package com.grapesapps.myapplication.view.screens.headphone

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import com.grapesapps.myapplication.entity.HeadsetMainSetting
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.view.observeAsState
import com.grapesapps.myapplication.vm.SplashStatePermission
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.replaceAll
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
    var searchHear by remember { mutableStateOf(false) }
    var autoAnswer by remember { mutableStateOf(false) }

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
                viewModel.load()
//                viewModel.viewStateHeadphone.
            }
        }
    )

    LaunchedEffect(key1 = lifecycleState) {
        when (lifecycleState) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.load()
            }
            else -> Unit
        }
    }

    when (val state = state.value) {
        is HeadphoneState.HeadphoneStateLoaded -> {

        }

        else -> {}
    }

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
                            Log.e("STATE", "${state.value}")
                            when (val state = state.value) {
                                is HeadphoneState.HeadphoneStateLoaded -> {
                                    HeadSetInfo(
                                        firstItemVisible,
                                        state.isConnected,
                                        l = state.leftHeadsetStatus,
                                        r = state.rightHeadsetStatus,
                                        c = state.caseHeadsetStatus,
                                    )
                                }
                                else -> {}
                            }

                        }
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(Modifier.fillMaxSize()) {
                                    Text("Noise Control", Modifier.padding(15.dp), fontSize = 22.sp)
                                    when (val state = state.value) {
                                        is HeadphoneState.HeadphoneStateLoaded -> {
                                            HeadphoneModeControl(
                                                isConnected = state.isConnected,
                                                mainHeadsetValue = state.mainHeadsetValue,
                                                onCheckedChange = {
                                                    viewModel.changeMainSetting(it, state)
                                                }
                                            )
                                            when (state.headsetStatus?.setting) {
                                                HeadsetMainSetting.Noise -> {
                                                    NoiseCancellationControl(
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
                                                        //  switchChecked = state.headsetStatus.value == 1
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
                                                                checked = false,
                                                                colors = SwitchDefaults.colors(),
                                                                onCheckedChange = {
                                                                    //  viewModel.changeMainSetting(it, state)
                                                                    // switchChecked = it
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
                                        }
                                        else -> {}
                                    }

                                    Text(
                                        "Пространственное звучание",
                                        Modifier.padding(start = 15.dp, end = 15.dp, top = 20.dp, bottom = 5.dp),
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(
                                        modifier = Modifier
                                            .padding(start = 15.dp, bottom = 10.dp, end = 10.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column() {
                                            Text("Объемный звук", fontSize = 14.sp)
                                            Text(
                                                "Статичное объемное звучание.Статичное объемное звучание.Статичное объемное звучание.",
                                                fontSize = 10.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                        Column() {
                                            Switch(
                                                modifier = Modifier
                                                    .scale(0.8f)
                                                    .padding(end = 15.dp),
                                                checked = switchChecked,
                                                colors = SwitchDefaults.colors(

                                                ),
                                                onCheckedChange = {
                                                    viewModel.onSelectSurroundAudio(isEnabled = it)
                                                }

                                            )
                                            Text("1")
                                            Spacer(modifier = Modifier.width(15.dp))
                                        }

                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 15.dp, bottom = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                        ) {
                                            Text("Отслеживание движения головы", fontSize = 14.sp)
                                            Text("Включение отслеживания движения головы.", fontSize = 10.sp)

                                        }
                                        Switch(
                                            modifier = Modifier
                                                .scale(0.8f)
                                                .padding(end = 15.dp),
                                            checked = switchChecked,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = {
                                                viewModel.onSelectSurroundAudio(isEnabled = it)
                                            }

                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 15.dp, bottom = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                        ) {
                                            Text("Улучшить объемный звук [BETA]", fontSize = 14.sp)
                                            Text("Может работать нестабильно и не со всеми плеерами", fontSize = 10.sp)
                                            Text("Необходимо перезапустить текущий плеер.", fontSize = 10.sp)
                                        }
                                        Switch(
                                            modifier = Modifier
                                                .scale(0.8f)
                                                .padding(end = 15.dp),
                                            checked = switchChecked,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = {
                                                viewModel.onSelectSurroundAudio(isEnabled = it)
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
                                            checked = searchHear,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = {
                                                viewModel.onSelectAutoSearchEar(isEnabled = it)
                                                searchHear = it
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
                                            checked = autoAnswer,
                                            colors = SwitchDefaults.colors(

                                            ),
                                            onCheckedChange = {
                                                viewModel.onSelectAutoPhoneAnswer(isEnabled = it)
                                                autoAnswer = it
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
                                            .clickable {
                                                navController.navigate(Screen.SettingScreen)
                                            }
                                            .height(60.dp)
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Настройки", fontSize = 14.sp)
                                        Icon(Icons.Filled.ArrowForwardIos, "ArrowForwardIos", Modifier.scale(0.7f))

                                    }

                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                navController.navigate(Screen.CheckHeadphoneScreen)
                                            }
                                            .height(60.dp)
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp),
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
                                "Отменить сопряжение",
                                fontSize = 16.sp,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 5.dp, top = 70.dp)
                                    .clickable {
                                        viewModel.removeBond()
                                        navController.replaceAll(Screen.SplashScreen)
                                    },
                            )

                        }
                        when (val state = state.value) {
                            is HeadphoneState.HeadphoneStateLoaded -> {
                                if (state.fwInfo != null) {
                                    item {
                                        Text(
                                            "Версия прошивки ${state.fwInfo.version}",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.Center)
                                                .padding(
                                                    start = 15.dp,
                                                    end = 15.dp,
                                                    top = 10.dp,
                                                    bottom = 40.dp
                                                ),
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        )
    }

}