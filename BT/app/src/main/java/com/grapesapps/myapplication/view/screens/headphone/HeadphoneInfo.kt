package com.grapesapps.myapplication.view.screens.headphone

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.grapesapps.myapplication.R
import com.grapesapps.myapplication.entity.CHeadsetBatteryStatus
import com.grapesapps.myapplication.entity.LHeadsetBatteryStatus
import com.grapesapps.myapplication.entity.RHeadsetBatteryStatus



@Composable
fun HeadSetInfo(
    isVisibleImage: Boolean,
    isConnected: Boolean,
    l: LHeadsetBatteryStatus?,
    r: RHeadsetBatteryStatus?,
    c: CHeadsetBatteryStatus?,
) {
    Log.e("ISCONNECTEDDEVICE", "$isConnected")
    val connectMessage = if (isConnected) "connected" else "disconnected"
    var left: String = ""
    var right: String = ""
    var case: String = ""
    if (l != null && l.battery != "-") {
        left = "L:${if (l.isCharging) "🔋" else ""}${l.battery}"
    }
    if (r != null && r.battery != "-") {
        right = "R:${if (r.isCharging) "🔋" else ""}${r.battery}"
    }
    if (c != null && c.battery != "-") {
        case = "C:${if (c.isCharging) "🔋" else ""}${c.battery}"
    }
    Box(
        modifier = Modifier
            .height(60.dp)
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp))
            .background(color = if (isVisibleImage) MaterialTheme.colorScheme.surface.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (isVisibleImage) 0.dp else 15.dp, vertical = 5.dp)
        ) {
            if (isVisibleImage) {
                val image = painterResource(R.drawable.headsetimage)
                Image(
                    painter = image,
                    contentDescription = null,
                    Modifier
                        .height(60.dp)
                        .width(60.dp)
                        .fillMaxSize()
                        .padding(start = 10.dp, top = 5.dp, bottom = 5.dp, end = 10.dp),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit
                )
            }

            Column() {
                Text(text = "Device $connectMessage", color = Color.White)
                Text(text = "$case $left $right", color = Color.White)
            }
        }
    }
}


