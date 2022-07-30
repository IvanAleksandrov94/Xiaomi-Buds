package com.grapesapps.myapplication.view.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

import com.grapesapps.myapplication.HomeScreen
import com.grapesapps.myapplication.vm.Home


@ExperimentalUnsignedTypes
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun Navigation(
    navController: NavController?,
) {
    val enterLaunchTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) =
        {
            fadeIn(tween(500))
        }


    val startDestination = AppScreens.HomeScreen.route

    AnimatedNavHost(
        navController = navController as NavHostController,
        startDestination = startDestination,
        enterTransition = {
            defaultPlumbusEnterTransition(
                initialState,
                targetState
            )
        },
        exitTransition = {
            defaultPlumbusExitTransition(
                initialState,
                targetState
            )
        },
        popEnterTransition = { defaultPlumbusPopEnterTransition() },
        popExitTransition = { defaultPlumbusPopExitTransition() },
    ) {

//        // Splash transition
//        composable(
//            route = AppScreens.SplashScreen.route,
//            enterTransition = enterLaunchTransition,
//        ) {
//            BackHandler(true) {}
//            val dailyViewModel = hiltViewModel<Splash>()
//            SplashView(navController = navController, viewModel = dailyViewModel, name = name)
//        }

        // HomeScreen transition
        composable(
            route = AppScreens.HomeScreen.route,
            enterTransition = enterLaunchTransition,
        ) {
            BackHandler(true) {}

            val dailyViewModel = hiltViewModel<Home>()
            HomeScreen(navController = navController, viewModel = dailyViewModel)

        }

//        // Auth & Registration transition
//        composable(
//            route = AppScreens.AuthScreen.route,
//            enterTransition = enterLaunchTransition,
//        ) {
//            BackHandler(true) {}
//            val viewModel = hiltViewModel<Auth>()
//            AuthScreen(navController = navController, viewModel)
//        }
//        composable(
//            route = AppScreens.RegistrationScreen.route,
//        ) {
//            RegistrationScreen(navController = navController)
//        }
//        composable(
//            route = AppScreens.ConfirmRegistrationScreen.route,
//        ) {
//            ConfirmRegistrationScreen(navController = navController)
//        }
//
//        // Invoices transition
//        composable(
//            route = AppScreens.InvoicesScreen.route,
//        ) {
//            val viewModel = hiltViewModel<Invoices>()
//            InvoicesScreen(navController = navController, viewModel = viewModel)
//        }
//        composable(
//            route = AppScreens.OpenedPositionScreen.route,
//        ) {
//            val viewModel = hiltViewModel<OpenedInvoice>()
//
//            OpenedPositionScreen(navController = navController, viewModel = viewModel)
//        }
//
//        // Bids transition
//        composable(
//            route = AppScreens.BidsScreen.route,
//        ) {
//            val viewModel = hiltViewModel<Bids>()
//            BidsScreen(navController = navController, viewModel= viewModel)
//        }
//        composable(
//            route = AppScreens.OpenedBidScreen.route,
//        ) {
//            val viewModel = hiltViewModel<OpenedBid>()
//            OpenedBidScreen(navController = navController, viewModel = viewModel)
//        }
//        // Other's transitions
//        composable(
//            route = AppScreens.AboutScreen.route,
//        ) {
//            AboutScreen(navController = navController)
//        }
//        composable(
//            route = AppScreens.FeedbackScreen.route,
//        ) {
//            val viewModel = hiltViewModel<Feedback>()
//            FeedbackScreen(navController = navController, viewModel = viewModel)
//        }
//        composable(
//            route = AppScreens.ManagerSendScreen.route,
//        ) {
//            val viewState = hiltViewModel<ManagerSend>()
//            ManagerSendScreen(navController = navController, viewModel = viewState)
//        }
    }
}


@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultPlumbusEnterTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): EnterTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph
    if (initialNavGraph.id != targetNavGraph.id) {
        return fadeIn()
    }
    return fadeIn() + slideIntoContainer(
        AnimatedContentScope.SlideDirection.Start,
        tween(350)
    )
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultPlumbusExitTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): ExitTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph
    if (initialNavGraph.id != targetNavGraph.id) {
        return fadeOut()
    }
    return fadeOut() + slideOutOfContainer(
        AnimatedContentScope.SlideDirection.Start,
        tween(350)
    )
}

private val NavDestination.hostNavGraph: NavGraph
    get() = hierarchy.first { it is NavGraph } as NavGraph

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultPlumbusPopEnterTransition(): EnterTransition {
    return fadeIn() + slideIntoContainer(
        AnimatedContentScope.SlideDirection.End,
        tween(350)
    )
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultPlumbusPopExitTransition(): ExitTransition {
    return fadeOut() + slideOutOfContainer(
        AnimatedContentScope.SlideDirection.End,
        tween(350)
    )
}