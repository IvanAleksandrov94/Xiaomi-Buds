package com.grapesapps.myapplication.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grapesapps.myapplication.model.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class HomeState {
    object HomeStateInitial : HomeState()
    object HomeStateExitAccount : HomeState()
    object HomeStateLoaded
        //(
//        val userModel: UserModel?,
//        val baseModel: BaseModel?,
 //   )
    : HomeState()

    data class HomeStateError(
        val message: String?
    ) : HomeState()

}

@HiltViewModel
class Home @Inject constructor(
    @ApplicationContext appContext: Context,
//    private val userRepository: UserRepository,
//    private val baseRepository: BaseRepository,
) : ViewModel() {
    private val _viewState: MutableLiveData<HomeState> = MutableLiveData(HomeState.HomeStateInitial)
    val viewState: LiveData<HomeState> = _viewState
    private val sharedPrefManager: SharedPrefManager

    init {
      //  DaggerRepositoryComponent.create().injectHome(this)
        sharedPrefManager = SharedPrefManager(appContext)
    }

    fun exitAccount() {
        viewModelScope.launch(Dispatchers.IO) {
//            sharedPrefManager.removeAuthToken()
//            sharedPrefManager.removeUserName()
//            userRepository.clearUser()
            _viewState.postValue(HomeState.HomeStateExitAccount)
            delay(100)
            _viewState.postValue(HomeState.HomeStateInitial)
        }
    }

    fun loadInfo() {
        viewModelScope.launch(Dispatchers.IO) {
//            val user = userRepository.getCurrentUser()
//            val base = baseRepository.getBaseModel()
            _viewState.postValue(HomeState.HomeStateLoaded)
        }
    }
}