package com.example.funfriday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.funfriday.ui.navigation.AppNavGraph
import com.example.funfriday.ui.theme.FunFridayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FunFridayTheme {
                AppNavGraph()
            }
        }
    }
}
