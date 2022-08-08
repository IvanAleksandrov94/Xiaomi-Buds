package com.grapesapps.myapplication.view.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import com.grapesapps.myapplication.bluetooth.BluetoothSDKListenerHelper
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import com.grapesapps.myapplication.bluetooth.IBluetoothSDKListener
import com.grapesapps.myapplication.observeAsState
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.view.navigation.Screen
import com.grapesapps.myapplication.vm.HomeState
import com.grapesapps.myapplication.vm.Splash
import com.grapesapps.myapplication.vm.SplashState
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.navigate
import kotlinx.coroutines.*
import okhttp3.internal.wait


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
    viewModel: Splash,
    navController: NavController<Screen>,
) {
    val state: State<SplashState?> = viewModel.viewStateSplash.observeAsState()
    val context = LocalContext.current
    val lifecycleStateObserver = LocalLifecycleOwner.current.lifecycle.observeAsState()
    val lifecycleState = lifecycleStateObserver.value

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
            }
        }
    )

    LaunchedEffect(key1 = lifecycleState) {
        when (lifecycleState) {
            Lifecycle.Event.ON_RESUME -> {
                print("!!")
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
            Lifecycle.Event.ON_DESTROY -> {
                print("!!")
            }
            else -> Unit
        }
    }

    DisposableEffect(key1 = viewModel) {
        onDispose {
            val connection = viewModel.getServiceConnection()
            context.unbindService(connection);
        }
    }
    when (val s = state.value) {
        is SplashState.SplashSuccessNavigate -> {
            navController.navigate(Screen.HomeScreen)
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

//    var stateSearch by remember { mutableStateOf(true) }
    val stateOpenBluetoothText  = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    var stateAnim by remember { mutableStateOf(true) }


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
                            is SplashState.SplashBluetoothDisabled -> {
                                AnimatedVisibility(
                                    visibleState = stateMainTextBluetooth,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 2000)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 1))
                                ) {
                                    Text(
                                        text = "Включите Bluetooth",
                                        fontSize = 30.sp,
                                        modifier = Modifier.padding(bottom = 35.dp)
                                    )
                                }
                            }
                            is SplashState.SplashSuccessConnected -> {
                                AnimatedVisibility(
                                    visibleState = stateMainTextDeviceName,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 3000)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 1))
                                ) {
                                    Text(
                                        text = splashState.deviceName,
                                        fontSize = 30.sp,
                                        modifier = Modifier.padding(bottom = 35.dp)
                                    )
                                }
                            }
                            is SplashState.SplashReceiverStartSearch -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
//                                    AnimatedVisibility(
//                                        visibleState = stateMainText,
//                                        enter = fadeIn(animationSpec = tween(durationMillis = 2000)),
//                                        exit = fadeOut(animationSpec = tween(durationMillis = 1))
//                                    ) {
                                    Text(
                                        text = "Подключите наушники",
                                        fontSize = 30.sp,
                                        modifier = Modifier.padding(bottom = 35.dp)
                                    )
                                    //}
                                    LoadingIndicator(
                                        animating = true,
                                        modifier = Modifier.graphicsLayer { alpha = if (stateAnim) 1f else 0f },
                                        color = MaterialTheme.colorScheme.onSurface,
                                        indicatorSpacing = 5.dp,
                                        animationType = AnimationType.Bounce,
                                    )
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
                            is SplashState.SplashSuccessNavigate -> {

                            }
                            is SplashState.SplashStateIdle -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AnimatedVisibility(
                                        visibleState = stateMainText,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 2000)),
                                        exit = fadeOut(animationSpec = tween(durationMillis = 1))
                                    ) {
                                        Text(
                                            text = "Подключите наушники",
                                            fontSize = 30.sp,
                                            modifier = Modifier.padding(bottom = 35.dp)
                                        )
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

                        Log.e("STATE", "${state.value}" )

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
//                                    AnimatedVisibility(
//                                        visibleState = stateSearch,
//                                        enter = fadeIn(animationSpec = tween(durationMillis = 3000)),
//                                        exit = fadeOut(animationSpec = tween(durationMillis = 1))
//                                    ) {
//                                        Text(
//                                            text = "Найти наушники?",
//                                            fontSize = 18.sp,
//                                            modifier = Modifier
//                                                .padding(bottom = 35.dp, start = 5.dp, end = 5.dp, top = 5.dp)
//                                                .clickable(
//                                                    interactionSource = MutableInteractionSource(),
//                                                    indication = null,
//                                                    onClick = {
//                                                        stateSearch.targetState = false
//                                                        stateAnim = true
//                                                        viewModel.startSearchReceiver()
//                                                    }
//                                                )
//                                        )
//                                    }
                                }
                                is SplashState.SplashReceiverStartSearch -> {
                                    AnimatedVisibility(
                                        visible = !stateSearch.targetState && !stateOpenBluetoothText.targetState,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 2000), initialAlpha = 0f),
                                        exit = fadeOut(animationSpec = tween(durationMillis = 2000), targetAlpha = 0f)
                                    ) {
                                        Text(
                                            text = "Идет поиск...",
                                            fontSize = 18.sp,
                                            modifier = Modifier
                                                .padding(bottom = 35.dp, start = 5.dp, end = 5.dp, top = 5.dp)

                                        )
                                    }
                                }
                                is SplashState.SplashSuccessNavigate -> {

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
//
//@Preview(widthDp = 360, heightDp = 360)
//@Composable
//private fun PreviewLoadingButton() {
//    LoadingButtonTheme {
//        var loading by remember {
//            mutableStateOf(false)
//        }
//        Surface(modifier = Modifier.fillMaxSize()) {
//            LoadingButton(
//                onClick = { loading = !loading },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(all = 16.dp),
//                loading = loading,
//            ) {
//                Text(
//                    text = "Refresh"
//                )
//            }
//        }
//    }
//}
//
//@Preview(widthDp = 200, heightDp = 200)
//@Composable
//fun IconPreview() {
//    LoadingButtonTheme {
//        Surface(modifier = Modifier.fillMaxSize()) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center,
//            ) {
//                Icon(
//                    Icons.Default.Add,
//                    modifier = Modifier
//                        .width(100.dp)
//                        .aspectRatio(1f),
//                    contentDescription = null,
//                )
//            }
//        }
//    }
//}