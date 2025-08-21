package com.example.buyme.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.buyme.ui.screens.CategoryOverviewScreen

object Destinations {
    const val Overview = "overview"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Destinations.Overview) {
        composable(Destinations.Overview) {
            CategoryOverviewScreen()
        }
    }
}
