package com.grapesapps.myapplication.model
import com.grapesapps.myapplication.vm.HeadphoneVm
import com.grapesapps.myapplication.vm.Splash
import dagger.Component

@Component
interface RepositoryComponent {
    fun injectHeadphone(headphoneVm: HeadphoneVm)
    fun injectSplash(splash: Splash)
}