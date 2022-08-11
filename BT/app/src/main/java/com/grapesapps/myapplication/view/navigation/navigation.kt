import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import com.grapesapps.myapplication.view.navigation.Screen
import com.grapesapps.myapplication.view.screens.CheckHeadPhoneScreen
import com.grapesapps.myapplication.view.screens.SettingScreen
import com.grapesapps.myapplication.view.screens.headphone.HeadphoneScreen
import com.grapesapps.myapplication.view.screens.SplashScreen
import com.grapesapps.myapplication.vm.HeadphoneVm
import com.grapesapps.myapplication.vm.Splash
import dev.olshevski.navigation.reimagined.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavHostScreen(
    splashVm: Splash,
    headphoneVm: HeadphoneVm,
) {
    val navController = rememberNavController<Screen>(
        startDestination = Screen.SplashScreen
    )

    NavBackHandler(navController)

    AnimatedNavHost(controller = navController, transitionSpec = MainNavHostTransitionSpec) { screen ->
        when (screen) {
            is Screen.SplashScreen -> SplashScreen(navController = navController, viewModel = splashVm)
            is Screen.HeadphoneScreen -> HeadphoneScreen(navController = navController, viewModel = headphoneVm)
            is Screen.SettingScreen -> SettingScreen(navController = navController)
            is Screen.CheckHeadphoneScreen -> CheckHeadPhoneScreen(navController = navController)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private val MainNavHostTransitionSpec =
    AnimatedNavHostTransitionSpec<Screen> { action, from, _ ->

        when (from) {
            Screen.SplashScreen -> {
                val outDuration = 100
                fadeIn(
                    animationSpec = tween(durationMillis = 200, delayMillis = outDuration)
                ) with fadeOut(
                    animationSpec = tween(durationMillis = outDuration)
                ) + scaleOut(
                    targetScale = 2f,
                    animationSpec = tween(durationMillis = outDuration)
                )
            }
            Screen.SettingScreen -> {
                if (action == NavAction.Navigate) {
                    slideIntoContainer(AnimatedContentScope.SlideDirection.Start) with fadeOut()
                } else {
                    fadeIn() with fadeOut()
                }
            }
            Screen.CheckHeadphoneScreen -> {
                if (action == NavAction.Navigate) {
                    slideIntoContainer(AnimatedContentScope.SlideDirection.Start) with fadeOut()
                } else {
                    fadeIn() with fadeOut()
                }
            }
            Screen.HeadphoneScreen -> {
                if (action == NavAction.Navigate) {
                    slideIntoContainer(AnimatedContentScope.SlideDirection.Start) with fadeOut()
                } else {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 500, delayMillis = 0
                        )
                    ) with fadeOut()
                }
            }
        }
    }
