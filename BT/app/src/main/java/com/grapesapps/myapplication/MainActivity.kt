@file:OptIn(ExperimentalComposeUiApi::class)

package com.grapesapps.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.grapesapps.myapplication.model.SharedPrefManager
import com.grapesapps.myapplication.view.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
@OptIn(ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var pref: SharedPrefManager

    @RequiresApi(Build.VERSION_CODES.N)
    @OptIn(ExperimentalUnsignedTypes::class, ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.WAKE_LOCK,
                    ),
                1
            )
        }

        val btClassicReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.e("BroadcastReceiver", "${intent.action}")

                val sessionStates = arrayOf(
                    AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION,
                    AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION,
                    AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL,
                )

                if (sessionStates.contains(intent.action)) {
                    val sessionID = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
                    if (sessionID == -1 || sessionID == -3) {
                        Log.e("BroadcastReceiver", "ERROR")
                        return
                    }
                    val mainEqualizer = Equalizer(100, sessionID)

                    val numberOfBands = mainEqualizer.numberOfBands
                    val bands = ArrayList<Int>(0)

                    val lowestBandLevel = mainEqualizer.bandLevelRange?.get(0)
                    val highestBandLevel = mainEqualizer.bandLevelRange?.get(1)

                    (0 until numberOfBands)
                        .map { mainEqualizer.getCenterFreq(it.toShort()) }
                        .mapTo(bands) { it.div(1000) }
                        .forEachIndexed { index, it ->
                            Log.d("TAG", "Center frequency: $it Hz")
                            if (it < 100) {
                                mainEqualizer.setBandLevel(index.toShort(), ((highestBandLevel?.div(1.3)) ?: 0).toShort())
                            } else if (it in 100..599) {
                                mainEqualizer.setBandLevel(index.toShort(), ((lowestBandLevel?.div(5)) ?: 0).toShort())
                            } else if (it in 600..2499) {
                                mainEqualizer.setBandLevel(index.toShort(), ((highestBandLevel?.div(5)) ?: 0).toShort())
                            } else if (it in 2500..6499) {
                                mainEqualizer.setBandLevel(
                                    index.toShort(),
                                    ((highestBandLevel?.div(2.8)) ?: 0).toShort()
                                )
                            } else if (it > 6499) {
                                mainEqualizer.setBandLevel(
                                    index.toShort(),
                                    ((highestBandLevel?.div(2.1)) ?: 0).toShort()
                                )
                            } else {
                                mainEqualizer.setBandLevel(index.toShort(), 0)
                            }
                            val level = mainEqualizer.getBandLevel(index.toShort())
                            Log.e("LEVEL", "CURRENT LEVEL: $level")
                        }
                    mainEqualizer.enabled = true
                }
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.EXTRA_CONTENT_TYPE)
        }
        registerReceiver(btClassicReceiver, intentFilter)

        setContent {
            pref = SharedPrefManager(LocalContext.current)
            navController = rememberAnimatedNavController()

            Navigation(
                navController = navController,
            )
        }
    }
}

