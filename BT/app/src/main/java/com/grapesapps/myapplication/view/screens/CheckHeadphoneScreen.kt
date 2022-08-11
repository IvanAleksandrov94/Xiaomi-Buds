package com.grapesapps.myapplication.view.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.view.navigation.Screen
import dev.olshevski.navigation.reimagined.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckHeadPhoneScreen(
    navController: NavController<Screen>,
) {
    BudsApplicationTheme {
        Scaffold(
            content = { contentPadding ->
                Box(
                    modifier = Modifier.padding(contentPadding)
                ) {
                    Text("asdadasd")

                }

            }
        )

    }

}