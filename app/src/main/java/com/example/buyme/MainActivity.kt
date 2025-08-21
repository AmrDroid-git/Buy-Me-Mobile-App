package com.example.buyme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.buyme.navigation.AppNavHost
import com.example.buyme.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val nav = rememberNavController()
                AppNavHost(navController = nav)
            }
        }
    }
}
