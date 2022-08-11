package com.grapesapps.myapplication

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.wearable.*

sealed class WearState {
    object WearStateInitial : WearState()
    object WearStateNeedConnect : WearState()
    class WearStateLoaded(
        val isConnected: Boolean,
        val mainHeadsetValue: Int = -1,
//        val leftHeadsetStatus: LHeadsetBatteryStatus?,
//        val rightHeadsetStatus: RHeadsetBatteryStatus?,
//        val caseHeadsetStatus: CHeadsetBatteryStatus?,
//        val headsetStatus: HeadsetSettingStatus?,
//        val fwInfo: FirmwareInfo?,
    ) : WearState()

    class WearStateError(
        val message: String?
    ) : WearState()

}

class ClientDataViewModel(
    application: Application
) :
    AndroidViewModel(application),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    companion object {
        private const val WATCH_UPDATE_INFO = "/watch_update"
    }

    private val _events = mutableStateListOf<Event>()
    private val viewState: MutableLiveData<WearState> = MutableLiveData(WearState.WearStateInitial)
    val state: LiveData<WearState> = viewState

    val events: List<Event> = _events

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        // Add all events to the event log
        Log.e("EVENTS WEAR", "${dataEvents.map { it.dataItem }}")

//        _events.addAll(
//            dataEvents.map { dataEvent ->
//                val title = when (dataEvent.type) {
//                    DataEvent.TYPE_CHANGED -> R.string.data_item_changed
//                    DataEvent.TYPE_DELETED -> R.string.data_item_deleted
//                    else -> R.string.data_item_unknown
//                }
//
//                Event(
//                    title = title,
//                    text = dataEvent.dataItem.toString()
//                )
//            }
//        )
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.e("EVENTS", messageEvent.path)
        when (messageEvent.path) {
            WATCH_UPDATE_INFO -> {
                Log.e("WATCH_UPDATE_INFO", "${messageEvent.data.map { it }}")
            }
        }


    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        _events.add(
            Event(
                title = R.string.capability_changed,
                text = capabilityInfo.toString()
            )
        )
    }
}

data class Event(
    @StringRes val title: Int,
    val text: String
)
