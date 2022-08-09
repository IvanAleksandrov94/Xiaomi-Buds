package com.grapesapps.myapplication.view

sealed class AppScreens(val route: String) {
    // Splash route
    object SplashScreen : AppScreens("splash_screen")

    // HeadphoneScreen route
    object HeadphoneScreen : AppScreens("headphone_screen")
}