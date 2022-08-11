package com.grapesapps.myapplication.view.navigation

import kotlinx.parcelize.Parcelize
import android.os.Parcelable


sealed class Screen : Parcelable {
    @Parcelize
    object SplashScreen : Screen()

    @Parcelize
    object HeadphoneScreen : Screen()

    @Parcelize
    object SettingScreen : Screen()

    @Parcelize
    object CheckHeadphoneScreen : Screen()
}