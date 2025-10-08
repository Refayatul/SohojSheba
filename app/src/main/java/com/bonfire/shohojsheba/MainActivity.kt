package com.bonfire.shohojsheba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bonfire.shohojsheba.navigation.AppNavGraph
import com.bonfire.shohojsheba.ui.theme.ShohojShebaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShohojShebaTheme {
                AppNavGraph()
            }
        }
    }
}
