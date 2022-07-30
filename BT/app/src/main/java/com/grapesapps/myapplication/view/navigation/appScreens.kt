package com.grapesapps.myapplication.view.navigation

sealed class AppScreens(val route: String) {
    // Splash route
    object SplashScreen : AppScreens("splash_screen")

    // HomeScreen route
    object HomeScreen : AppScreens("main_screen")
}