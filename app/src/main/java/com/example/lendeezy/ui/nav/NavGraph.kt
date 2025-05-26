// Used CoPilot for Debugging only on this page (dependency import error)

package com.example.lendeezy.ui.nav

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lendeezy.ui.screen.AddScreen
import com.example.lendeezy.ui.screen.HomeScreen
import com.example.lendeezy.ui.screen.LoginScreen
import com.example.lendeezy.ui.screen.UserScreen
import com.example.lendeezy.ui.viewmodel.AddProductViewModel
import com.example.lendeezy.ui.viewmodel.GetProductsViewModel
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel

/**
 * Controls navigation
 * Starts at Login page
 */
@Composable
fun NavGraph(startDestination: String, userViewModel: UserViewModel) {
    val navController = rememberNavController()

    // defines routes with dynamic start destination
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(userViewModel)
        }
        composable("home") {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = { NavBar(navController) }
            ) { paddingValues ->
                val getProductsViewModel: GetProductsViewModel = viewModel()
                HomeScreen(getProductsViewModel, userViewModel, paddingValues)
            }
        }
        composable("add") {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = { NavBar(navController) }
            ) { paddingValues ->
                // send add product view model in
                val addViewModel: AddProductViewModel = viewModel()
                AddScreen(addViewModel, paddingValues)
            }
        }
        composable("user") {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = { NavBar(navController) }
            ) { paddingValues ->
                UserScreen(userViewModel, paddingValues)
            }
        }
    }
}
