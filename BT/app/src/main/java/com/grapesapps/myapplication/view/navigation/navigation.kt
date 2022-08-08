import androidx.compose.animation.*
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import com.grapesapps.myapplication.HeadsetScreen
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.view.navigation.Screen
import com.grapesapps.myapplication.view.screens.SplashScreen
import com.grapesapps.myapplication.vm.Home
import com.grapesapps.myapplication.vm.Splash
import dev.olshevski.navigation.reimagined.*

//package com.grapesapps.myapplication.view.navigation
//
//import android.util.Log
//import androidx.activity.compose.BackHandler
//import androidx.compose.animation.*
//import androidx.compose.animation.core.tween
//import androidx.compose.runtime.Composable
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.*
//import androidx.navigation.NavDestination.Companion.hierarchy
//import com.google.accompanist.navigation.animation.AnimatedNavHost
//import com.google.accompanist.navigation.animation.composable
//import com.grapesapps.myapplication.HeadsetScreen
//import com.grapesapps.myapplication.view.screens.SplashScreen
//import com.grapesapps.myapplication.vm.Home
//import com.grapesapps.myapplication.vm.Splash
//
//
//@ExperimentalAnimationApi
//@Composable
//fun Navigation(
//    navController: NavController,
//) {
//    val enterLaunchTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) =
//        {
//            fadeIn(tween(500))
//        }
//
//
//    val startDestination = AppScreens.SplashScreen.route
//
//    AnimatedNavHost(
//        navController = navController as NavHostController,
//        startDestination = startDestination,
//        enterTransition = {
//            defaultPlumbusEnterTransition(
//                initialState,
//                targetState
//            )
//        },
//        exitTransition = {
//            defaultPlumbusExitTransition(
//                initialState,
//                targetState
//            )
//        },
//        popEnterTransition = { defaultPlumbusPopEnterTransition() },
//        popExitTransition = { defaultPlumbusPopExitTransition() },
//    ) {
//        try {
//            // Splash transition
//            composable(
//                route = AppScreens.SplashScreen.route,
//                enterTransition = enterLaunchTransition,
//            ) {
//                BackHandler(true) {}
//                val dailyViewModel = hiltViewModel<Splash>()
//                SplashScreen(navController = navController, viewModel = dailyViewModel)
//            }
//
//            // HomeScreen transition
//            composable(
//                route = AppScreens.HomeScreen.route,
//                enterTransition = enterLaunchTransition,
//            ) {
//                BackHandler(true) { }
//                val dailyViewModel = hiltViewModel<Home>()
//                HeadsetScreen(navController = navController, viewModel = dailyViewModel)
//
//            }
//        } catch (e: Exception) {
//
//
//        }
//
//
////        // Auth & Registration transition
////        composable(
////            route = AppScreens.AuthScreen.route,
////            enterTransition = enterLaunchTransition,
////        ) {
////            BackHandler(true) {}
////            val viewModel = hiltViewModel<Auth>()
////            AuthScreen(navController = navController, viewModel)
////        }
////        composable(
////            route = AppScreens.RegistrationScreen.route,
////        ) {
////            RegistrationScreen(navController = navController)
////        }
////        composable(
////            route = AppScreens.ConfirmRegistrationScreen.route,
////        ) {
////            ConfirmRegistrationScreen(navController = navController)
////        }
////
////        // Invoices transition
////        composable(
////            route = AppScreens.InvoicesScreen.route,
////        ) {
////            val viewModel = hiltViewModel<Invoices>()
////            InvoicesScreen(navController = navController, viewModel = viewModel)
////        }
////        composable(
////            route = AppScreens.OpenedPositionScreen.route,
////        ) {
////            val viewModel = hiltViewModel<OpenedInvoice>()
////
////            OpenedPositionScreen(navController = navController, viewModel = viewModel)
////        }
////
////        // Bids transition
////        composable(
////            route = AppScreens.BidsScreen.route,
////        ) {
////            val viewModel = hiltViewModel<Bids>()
////            BidsScreen(navController = navController, viewModel= viewModel)
////        }
////        composable(
////            route = AppScreens.OpenedBidScreen.route,
////        ) {
////            val viewModel = hiltViewModel<OpenedBid>()
////            OpenedBidScreen(navController = navController, viewModel = viewModel)
////        }
////        // Other's transitions
////        composable(
////            route = AppScreens.AboutScreen.route,
////        ) {
////            AboutScreen(navController = navController)
////        }
////        composable(
////            route = AppScreens.FeedbackScreen.route,
////        ) {
////            val viewModel = hiltViewModel<Feedback>()
////            FeedbackScreen(navController = navController, viewModel = viewModel)
////        }
////        composable(
////            route = AppScreens.ManagerSendScreen.route,
////        ) {
////            val viewState = hiltViewModel<ManagerSend>()
////            ManagerSendScreen(navController = navController, viewModel = viewState)
////        }
//    }
//}
//
//
//@ExperimentalAnimationApi
//private fun AnimatedContentScope<*>.defaultPlumbusEnterTransition(
//    initial: NavBackStackEntry,
//    target: NavBackStackEntry,
//): EnterTransition {
//    val initialNavGraph = initial.destination.hostNavGraph
//    val targetNavGraph = target.destination.hostNavGraph
//    if (initialNavGraph.id != targetNavGraph.id) {
//        return fadeIn()
//    }
//    return fadeIn() + slideIntoContainer(
//        AnimatedContentScope.SlideDirection.Start,
//        tween(350)
//    )
//}
//
//@ExperimentalAnimationApi
//private fun AnimatedContentScope<*>.defaultPlumbusExitTransition(
//    initial: NavBackStackEntry,
//    target: NavBackStackEntry,
//): ExitTransition {
//    val initialNavGraph = initial.destination.hostNavGraph
//    val targetNavGraph = target.destination.hostNavGraph
//    if (initialNavGraph.id != targetNavGraph.id) {
//        return fadeOut()
//    }
//    return fadeOut() + slideOutOfContainer(
//        AnimatedContentScope.SlideDirection.Start,
//        tween(350)
//    )
//}
//
//private val NavDestination.hostNavGraph: NavGraph
//    get() = hierarchy.first { it is NavGraph } as NavGraph
//
//@ExperimentalAnimationApi
//private fun AnimatedContentScope<*>.defaultPlumbusPopEnterTransition(): EnterTransition {
//    return fadeIn() + slideIntoContainer(
//        AnimatedContentScope.SlideDirection.End,
//        tween(350)
//    )
//}
//
//@ExperimentalAnimationApi
//private fun AnimatedContentScope<*>.defaultPlumbusPopExitTransition(): ExitTransition {
//    return fadeOut() + slideOutOfContainer(
//        AnimatedContentScope.SlideDirection.End,
//        tween(350)
//    )
//}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavHostScreen(
    splashVm: Splash,
    headsetVm: Home,

    ) {
    val navController = rememberNavController<Screen>(
        startDestination = Screen.SplashScreen
    )

    NavBackHandler(navController)
//
//    val enterLaunchTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) =
//        {
//            fadeIn(tween(500))
//        }

    AnimatedNavHost(controller = navController, transitionSpec = MainNavHostTransitionSpec) { screen ->
        when (screen) {
            is Screen.SplashScreen -> SplashScreen(navController = navController, viewModel = splashVm)
            is Screen.HomeScreen -> HeadsetScreen(navController = navController, viewModel = headsetVm)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private val MainNavHostTransitionSpec =
    AnimatedNavHostTransitionSpec<Screen> { _, from, _ ->
        if (from == Screen.SplashScreen) {
            val outDuration = 100
            fadeIn(
                animationSpec = tween(durationMillis = 200, delayMillis = outDuration)
            ) with fadeOut(
                animationSpec = tween(durationMillis = 150)
            ) + scaleOut(
                targetScale = 2f,
                animationSpec = tween(durationMillis = outDuration)
            )
        } else {
            //  scaleIn() with scaleOut()
            fadeIn(tween()) with fadeOut(tween())
        }
    }