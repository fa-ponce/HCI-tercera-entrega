package com.example.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.ui.navigation.NavGraph
import com.example.smarthome.ui.theme.SmarthomeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmarthomeTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
