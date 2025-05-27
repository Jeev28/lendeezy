// Used CoPilot for Debugging only on this page (dependency import error)

package com.example.lendeezy.ui.nav

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lendeezy.ui.screen.AddScreen
import com.example.lendeezy.ui.screen.HomeScreen
import com.example.lendeezy.ui.screen.LoginScreen
import com.example.lendeezy.ui.screen.ProductScreen
import com.example.lendeezy.ui.screen.UserScreen
import com.example.lendeezy.ui.viewmodel.AddProductViewModel
import com.example.lendeezy.ui.viewmodel.GetProductsViewModel
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel

/**
 * Controls navigation
 * Starts at Login page
 */
@OptIn(ExperimentalMaterial3Api::class)
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
                HomeScreen(navController, getProductsViewModel, userViewModel, paddingValues)
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

        composable("product/{productId}") { backStackEntry ->
            // get product ID
            val productId = backStackEntry.arguments?.getString("productId") ?: ""

            // get product from firebase
            val context = LocalContext.current
            val productViewModel: GetProductsViewModel = viewModel()
            val selectedProduct by productViewModel.selectedProduct.collectAsState()
            LaunchedEffect(productId) {
                productViewModel.fetchProductById(productId)
            }

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                // bottom bar is the nav bar
                bottomBar = { NavBar(navController) },
                // top bar is a back button and title and android sharesheet
                topBar = {
                    TopAppBar(
                        title = { Text("Product Details") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        // Android Sharesheet
                        actions = {
                            if (selectedProduct != null) {
                                IconButton(onClick = {
                                    val product = selectedProduct!!
                                    // on click on share button, use Android Sharesheet
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            """
                                                View this product on Lendeezy:
                                                
                                                ${product.name}
                                                Location: ${product.location}
                                                
                                                View it here: lendeezy://product/${product.id}
                                            """.trimIndent()
                                        )
                                    }
                                    // share options using sharesheet
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Share product via")
                                    )
                                }) {
                                    // share icon inside button
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
                            }
                        }
                    )
                }
            ) { paddingValues ->
                ProductScreen(padding = paddingValues, productId = productId)
            }
        }
    }
}
