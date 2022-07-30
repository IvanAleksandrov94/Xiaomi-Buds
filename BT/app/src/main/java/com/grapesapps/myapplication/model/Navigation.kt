package com.grapesapps.myapplication.model
import com.grapesapps.myapplication.vm.Home
import dagger.Component

@Component
interface RepositoryComponent {
    fun injectHome(home: Home)
   // fun injectSplash(splash: Splash)
}