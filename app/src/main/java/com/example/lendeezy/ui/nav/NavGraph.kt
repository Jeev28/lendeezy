package com.example.lendeezy.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lendeezy.ui.screen.HomeScreen
import com.example.lendeezy.ui.screen.LoginScreen
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel

/**
 * Controls navigation
 * Starts at Login page
 */
@Composable
fun NavGraph(userViewModel: UserViewModel) {
    val navController = rememberNavController()
    val userState by userViewModel.userState.collectAsState()

    // navigate to Home screen when login is successful
    LaunchedEffect(userState) {
        when (userState) {
            // go to home after login
            is UserState.Success -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            // go to login after sign out
            is UserState.Idle -> {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
            else -> {} // nothing for loading or error states
        }


    }

    // define navigation graph
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(userViewModel)
        }
        composable("home") {
            HomeScreen(userViewModel)
        }
    }

}