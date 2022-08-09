package com.grapesapps.myapplication.view.screens.headphone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grapesapps.myapplication.R

@Composable
fun HeadSetImage(height: Dp) {
    val image = painterResource(R.drawable.headsetimage)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .height(height)
            .background(color = MaterialTheme.colorScheme.surface),
    ) {
        Image(
            painter = image,
            contentDescription = null,
            Modifier
                .fillMaxSize()
                .padding(horizontal = 50.dp, vertical = 25.dp),
            alignment = Alignment.TopCenter,
            contentScale = ContentScale.Fit
        )
    }
}
