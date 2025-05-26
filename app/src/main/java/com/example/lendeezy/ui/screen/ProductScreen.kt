package com.example.lendeezy.ui.screen

import androidx.compose.ui.unit.dp

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lendeezy.ui.viewmodel.GetProductsViewModel
import com.example.lendeezy.ui.viewmodel.ProductListState
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel


/**
 * Shows detailed view of a specific clicked on product
 */
@Composable
fun ProductScreen(padding: PaddingValues, productId: String, productViewModel: GetProductsViewModel = viewModel()) {
    val productState by productViewModel.productState.collectAsState()
    val selectedProduct by productViewModel.selectedProduct.collectAsState()

    // fetch the product details for product ID
    LaunchedEffect(productId) {
        productViewModel.fetchProductById(productId)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {


        when (productState) {
            // if loading, show spinner
            is ProductListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // if error, show error message
            is ProductListState.Error -> {
                Text("Failed to load product")
            }
            // if success, show details
            is ProductListState.Success -> {
                if (selectedProduct != null) {
                    Text("Product: ${selectedProduct!!.name}")
                } else {
                    Text("Failed to load product")
                }
            }
            ProductListState.Idle -> {
                Text("No product data yet.")
            }
        }
    }

}
