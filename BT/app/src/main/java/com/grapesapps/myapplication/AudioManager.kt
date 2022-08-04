package com.grapesapps.myapplication

import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioManagerService @Inject constructor() {
    companion object {
        private const val TAG = "BT_CONNECT"
    }
    private lateinit var outputStream: OutputStream

    init {

    }

    fun send(data: String) {

    }
}