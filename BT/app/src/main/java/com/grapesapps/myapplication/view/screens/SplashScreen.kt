package com.grapesapps.myapplication.view.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.compiler.plugins.kotlin.EmptyFunctionMetrics.packageName
import androidx.compose.compiler.plugins.kotlin.EmptyModuleMetrics.log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import com.google.common.reflect.Reflection.getPackageName
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import com.grapesapps.myapplication.bluetooth.BluetoothUtils
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.view.navigation.Screen
import com.grapesapps.myapplication.view.observeAsState
import com.grapesapps.myapplication.vm.Splash
import com.grapesapps.myapplication.vm.SplashState
import com.grapesapps.myapplication.vm.SplashStatePermission
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.replaceAll
import kotlinx.coroutines.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
    viewModel: Splash,
    navController: NavController<Screen>,
) {
    val state: State<SplashState?> = viewModel.viewStateSplash.observeAsState()
    val stateNavigate: State<Boolean?> = viewModel.viewStateSplashNavigate.observeAsState()
    val statePermission: State<SplashStatePermission?> = viewModel.viewStateSplashPermission.observeAsState()
    val context = LocalContext.current
    val lifecycleStateObserver = LocalLifecycleOwner.current.lifecycle.observeAsState()
    val lifecycleState = lifecycleStateObserver.value
    var isRequestedPermission by remember { mutableStateOf(false) }

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

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                val result = permissions
                    .filterKeys {
                        it == Manifest.permission.BLUETOOTH_CONNECT
                    }

                val deniedList: List<String> = result.filter {
                    !it.value
                }.map {
                    it.key
                }
                when {
                    deniedList.isNotEmpty() -> {
                        val map = deniedList.groupBy { permission ->
                            if (shouldShowRequestPermissionRationale(
                                    context as Activity,
                                    permission
                                )
                            ) "DENIED" else "EXPLAINED"
                        }
                        map["DENIED"]?.let {
                            isRequestedPermission = true
                            // viewModel.onRequestPermission()
                            print(it)
                        }
                        map["EXPLAINED"]?.let {
                            isRequestedPermission = true
                            // viewModel.onRequestPermission()
                            if (viewModel.viewStateSplashPermission.value is SplashStatePermission.SplashStatePermissionRequested) {
                                Toast.makeText(
                                    context as Activity,
                                    "Предоставьте разрешение к обнаружению устройств поблизости",
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivityForResult(context as Activity, intent, 1, Bundle())
                            }
                        }
                    }
                    else -> {
                        viewModel.onChangePermission(SplashStatePermission.SplashStatePermissionGranted)
                        if (isRequestedPermission) {
                            viewModel.load()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    context as Activity,
                    "Предоставьте разрешение к обнаружению устройств поблизости",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }

    LaunchedEffect(
        key1 = viewModel,
        block = {
            launch {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    isRequestedPermission = true
                    viewModel.onRequestPermission()
                }
            }
        }
    )

    LaunchedEffect(key1 = lifecycleState) {
        when (lifecycleState) {
            Lifecycle.Event.ON_RESUME -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (viewModel.viewStateSplashPermission.value is SplashStatePermission.SplashStatePermissionRequested) {
                        val isGranted = checkSelfPermission(
                            context as Activity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        )
                        if (isGranted == 0) {
                            viewModel.onChangePermission(SplashStatePermission.SplashStatePermissionGranted)
                            viewModel.loadBeforeRequestPermission()
                        } else {
                            viewModel.onChangePermission(SplashStatePermission.SplashStatePermissionRequested)
                        }
                    }
                    if (viewModel.viewStateSplashPermission.value is SplashStatePermission.SplashStatePermissionInitial) {
                        viewModel.onChangePermission(SplashStatePermission.SplashStatePermissionRequested)
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.FOREGROUND_SERVICE,
                                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                                Manifest.permission.WAKE_LOCK,
                            )
                        )
                    }
                }
            }
            else -> Unit
        }
    }

    DisposableEffect(key1 = viewModel) {
        onDispose {
            try {
                val connection = viewModel.getServiceConnection()
                context.unbindService(connection)
            } catch (e:Exception){
                Log.e("SplashScreen", "$e")
            }
        }
    }
    // Log.e("CURRENTSTATE", "${statePermission.value}")

    when (statePermission.value) {
        is SplashStatePermission.SplashStatePermissionGranted -> {
            val notifyIntent = Intent(context, BluetoothSDKService::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = "START_ACTION"
            }
            context.startService(notifyIntent)
            bindBluetoothService()
            viewModel.onChangePermission(SplashStatePermission.SplashStateSuccessLoaded)
        }

        else -> {}

    }

    val stateMainText = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    val stateMainTextBluetooth = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    val stateMainTextDeviceName = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    val stateSearch = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    val stateSearchProcess = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

//    var stateSearch by remember { mutableStateOf(true) }
    val stateOpenBluetoothText = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    val stateRequestPermission = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    var stateAnim by remember { mutableStateOf(true) }

    when (stateNavigate.value) {
        true -> {
            navController.replaceAll(Screen.HeadphoneScreen)
        }
        else -> {}
    }
    BudsApplicationTheme {
        Scaffold(
            content = { contentPadding ->
                Box(
                    modifier = Modifier.padding(contentPadding)
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Spacer(modifier = Modifier.padding(top = 100.dp))
                        when (val splashState = state.value) {
                            is SplashState.SplashRequestPermission -> {
                                AnimatedVisibility(
                                    visibleState = stateRequestPermission,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 2000)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 1))
                                ) {
                                    Box(
                                        Modifier
                                            .height(150.dp)
                                            .padding(horizontal = 15.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Предоставьте разрешения",
                                                fontSize = 26.sp,
                                                modifier = Modifier.padding(bottom = 35.dp),
                                                textAlign = TextAlign.Center

                                            )
                                            Text(
                                                text = "Чтобы подключить наушники необходимо дать доступ к устройствам по близости",
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(bottom = 35.dp),
                                                textAlign = TextAlign.Center

                                            )
                                        }
                                    }
                                }
                            }
                            is SplashState.SplashBluetoothDisabled -> {
                                AnimatedVisibility(
                                    visibleState = stateMainTextBluetooth,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 2000)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 1))
                                ) {
                                    Box(Modifier.height(100.dp)) {
                                        Text(
                                            text = "Включите Bluetooth",
                                            fontSize = 30.sp,
                                            modifier = Modifier.padding(bottom = 35.dp)
                                        )
                                    }
                                }
                            }
                            is SplashState.SplashSuccessConnected -> {
                                AnimatedVisibility(
                                    visibleState = stateMainTextDeviceName,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                                ) {
                                    Box(Modifier.height(100.dp)) {
                                        Text(
                                            text = splashState.deviceName,
                                            fontSize = 30.sp,
                                            modifier = Modifier.padding(bottom = 35.dp)
                                        )
                                    }
                                }
                            }
                            is SplashState.SplashReceiverStartSearch -> {
                                Box(Modifier.height(100.dp)) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Подключите наушники",
                                            fontSize = 30.sp,
                                            modifier = Modifier.padding(bottom = 35.dp)
                                        )
                                        LoadingIndicator(
                                            animating = true,
                                            modifier = Modifier.graphicsLayer { alpha = if (stateAnim) 1f else 0f },
                                            color = MaterialTheme.colorScheme.onSurface,
                                            indicatorSpacing = 5.dp,
                                            animationType = AnimationType.Bounce,
                                        )

                                    }
                                }
                            }
                            is SplashState.SplashStateInitial -> {
//                                Column(
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    AnimatedVisibility(
//                                        visibleState = stateMainText,
//                                        enter = fadeIn(animationSpec = tween(durationMillis = 2000)),
//                                        exit = fadeOut(animationSpec = tween(durationMillis = 1))
//                                    ) {
//                                        Text(
//                                            text = "Подключите наушники",
//                                            fontSize = 30.sp,
//                                            modifier = Modifier.padding(bottom = 35.dp)
//                                        )
//                                    }
//                                }
                            }
//                            is SplashState.SplashSuccessNavigate -> {
//
//                            }
                            is SplashState.SplashStateIdle -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AnimatedVisibility(
                                        visibleState = stateMainText,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 2000)),
                                        exit = fadeOut(animationSpec = tween(durationMillis = 1))
                                    ) {
                                        Box(Modifier.height(100.dp)) {
                                            Text(
                                                text = "Подключите наушники",
                                                fontSize = 30.sp,
                                                modifier = Modifier.padding(bottom = 35.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
//                                Text(
//                                    text = "Подключите наушники",
//                                    fontSize = 30.sp,
//                                    modifier = Modifier.padding(bottom = 35.dp)
//                                )
                            }
                        }

                        Log.e("STATE", "${state.value}")

                        Column(
                            Modifier.height(100.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (state.value) {
                                is SplashState.SplashBluetoothDisabled -> {
                                    AnimatedVisibility(
                                        visibleState = stateOpenBluetoothText,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 2000)),
                                        exit = fadeOut(animationSpec = tween(durationMillis = 1))
                                    ) {
                                        Text(
                                            text = "Открыть настройки Bluetooth",
                                            fontSize = 18.sp,
                                            modifier = Modifier
                                                .padding(bottom = 35.dp, start = 5.dp, end = 5.dp, top = 5.dp)
                                                .clickable(
                                                    interactionSource = MutableInteractionSource(),
                                                    indication = null,
                                                    onClick = {
                                                        startActivity(
                                                            context,
                                                            Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS),
                                                            Bundle()
                                                        )
                                                    }
                                                )
                                        )
                                    }
                                }
                                is SplashState.SplashStateInitial -> {
                                    Text(text = "")
                                }
                                is SplashState.SplashReceiverStartSearch -> {
                                    AnimatedVisibility(
                                        visibleState = stateSearchProcess,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 2000)),
                                        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
                                    ) {
                                        Text(
                                            text = "Идет поиск...",
                                            fontSize = 18.sp,
                                            modifier = Modifier
                                                .padding(bottom = 35.dp, start = 5.dp, end = 5.dp, top = 5.dp)

                                        )
                                    }
                                }
                                is SplashState.SplashRequestPermission -> {
                                    AnimatedVisibility(
                                        visibleState = stateRequestPermission,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 3000)),
                                        exit = fadeOut(animationSpec = tween(durationMillis = 1))
                                    ) {
                                        Text(
                                            text = "Предоставить",
                                            fontSize = 18.sp,
                                            modifier = Modifier
                                                .padding(bottom = 35.dp, start = 5.dp, end = 5.dp, top = 5.dp)
                                                .clickable(
                                                    interactionSource = MutableInteractionSource(),
                                                    indication = null,
                                                    onClick = {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                            requestPermissionLauncher.launch(
                                                                arrayOf(
                                                                    Manifest.permission.BLUETOOTH_CONNECT,
                                                                    Manifest.permission.BLUETOOTH_ADMIN,
                                                                    Manifest.permission.BLUETOOTH,
                                                                    Manifest.permission.BLUETOOTH_SCAN,
                                                                    Manifest.permission.FOREGROUND_SERVICE,
                                                                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                                                                    Manifest.permission.WAKE_LOCK,
                                                                )
                                                            )
                                                        }
                                                        // viewModel.onRequestPermanentDeniedPermission()
                                                    }
                                                )
                                        )
                                    }
                                }
                                is SplashState.SplashStateIdle -> {
                                    AnimatedVisibility(
                                        visibleState = stateSearch,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 3000)),
                                        exit = fadeOut(animationSpec = tween(durationMillis = 1))
                                    ) {
                                        Text(
                                            text = "Найти наушники?",
                                            fontSize = 18.sp,
                                            modifier = Modifier
                                                .padding(bottom = 35.dp, start = 5.dp, end = 5.dp, top = 5.dp)
                                                .clickable(
                                                    interactionSource = MutableInteractionSource(),
                                                    indication = null,
                                                    onClick = {
                                                        stateSearch.targetState = false
                                                        stateAnim = true
                                                        viewModel.startSearchReceiver()
                                                    }
                                                )
                                        )
                                    }
                                }
                                else -> {
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

enum class AnimationType {
    Bounce,
    LazyBounce,
    Fade,
}

private const val NumIndicators = 3
private const val IndicatorSize = 12
private const val BounceAnimationDurationMillis = 300
private const val FadeAnimationDurationMillis = 600

private val AnimationType.animationSpec: DurationBasedAnimationSpec<Float>
    get() = when (this) {
        AnimationType.Bounce,
        AnimationType.Fade -> tween(durationMillis = animationDuration)
        AnimationType.LazyBounce -> keyframes {
            durationMillis = animationDuration
            initialValue at 0
            0f at animationDuration / 4
            targetValue / 2f at animationDuration / 2
            targetValue / 2f at animationDuration
        }
    }

private val AnimationType.animationDuration: Int
    get() = when (this) {
        AnimationType.Bounce,
        AnimationType.LazyBounce -> BounceAnimationDurationMillis
        AnimationType.Fade -> FadeAnimationDurationMillis
    }

private val AnimationType.animationDelay: Int
    get() = animationDuration / NumIndicators

private val AnimationType.initialValue: Float
    get() = when (this) {
        AnimationType.Bounce -> IndicatorSize / 2f
        AnimationType.LazyBounce -> -IndicatorSize / 2f
        AnimationType.Fade -> 1f
    }

private val AnimationType.targetValue: Float
    get() = when (this) {
        AnimationType.Bounce -> -IndicatorSize / 2f
        AnimationType.LazyBounce -> IndicatorSize / 2f
        AnimationType.Fade -> .2f
    }

@Stable
interface LoadingIndicatorState {
    operator fun get(index: Int): Float

    fun start(animationType: AnimationType, scope: CoroutineScope)
}

class LoadingIndicatorStateImpl : LoadingIndicatorState {
    private val animatedValues = List(NumIndicators) { mutableStateOf(0f) }

    override fun get(index: Int): Float = animatedValues[index].value

    override fun start(animationType: AnimationType, scope: CoroutineScope) {
        repeat(NumIndicators) { index ->
            scope.launch {
                animate(
                    initialValue = animationType.initialValue,
                    targetValue = animationType.targetValue,
                    animationSpec = infiniteRepeatable(
                        animation = animationType.animationSpec,
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(animationType.animationDelay * index)
                    ),
                ) { value, _ -> animatedValues[index].value = value }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoadingIndicatorStateImpl

        if (animatedValues != other.animatedValues) return false

        return true
    }

    override fun hashCode(): Int {
        return animatedValues.hashCode()
    }
}

@Composable
fun rememberLoadingIndicatorState(
    animating: Boolean,
    animationType: AnimationType,
): LoadingIndicatorState {
    val state = remember {
        LoadingIndicatorStateImpl()
    }
    LaunchedEffect(key1 = Unit) {
        if (animating) {
            state.start(animationType, this)
        }
    }
    return state
}

@Composable
private fun LoadingIndicator(
    animating: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    indicatorSpacing: Dp = 5.dp,
    animationType: AnimationType,
) {
    val state = rememberLoadingIndicatorState(animating, animationType)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(NumIndicators) { index ->
            LoadingDot(
                modifier = Modifier
                    .padding(horizontal = indicatorSpacing)
                    .width(IndicatorSize.dp)
                    .aspectRatio(1f)
                    .then(
                        when (animationType) {
                            AnimationType.Bounce,
                            AnimationType.LazyBounce -> Modifier.offset(
                                y = state[index].coerceAtMost(
                                    IndicatorSize / 2f
                                ).dp
                            )
                            AnimationType.Fade -> Modifier.graphicsLayer { alpha = state[index] }
                        }
                    ),
                color = color,
            )
        }
    }
}

@Composable
private fun LoadingDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(shape = CircleShape)
            .background(color = color)
    )
}
