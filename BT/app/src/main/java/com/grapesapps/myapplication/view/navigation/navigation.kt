import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import com.grapesapps.myapplication.view.navigation.Screen
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
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private val MainNavHostTransitionSpec =
    AnimatedNavHostTransitionSpec<Screen> { n, from, e ->

        if (from == Screen.SplashScreen) {
            val outDuration = 100
            fadeIn(
                animationSpec = tween(durationMillis = 200, delayMillis = outDuration)
            ) with fadeOut(
                animationSpec = tween(durationMillis = outDuration)
            ) + scaleOut(
                targetScale = 2f,
                animationSpec = tween(durationMillis = outDuration)
            )
        } else {
            //  scaleIn() with scaleOut()
            fadeIn(
                animationSpec = tween(
                    durationMillis = 500, delayMillis = 0
                )
            ) with fadeOut(
                tween(0)
            )
        }
    }