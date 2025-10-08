package com.bonfire.sohojsheba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bonfire.sohojsheba.navigation.AppNavGraph
import com.bonfire.sohojsheba.ui.theme.SohojShebaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SohojShebaTheme {
                AppNavGraph()
            }
        }
    }
}
