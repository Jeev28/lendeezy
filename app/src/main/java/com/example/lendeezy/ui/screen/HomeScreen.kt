package com.example.lendeezy.ui.screen

import android.graphics.Paint.Align
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel
import coil.compose.AsyncImage
import com.example.lendeezy.data.model.Product
import com.example.lendeezy.ui.viewmodel.GetProductsViewModel
import com.example.lendeezy.ui.viewmodel.ProductListState

/**
 * Shows user details currently
 * Later change to be full list of products
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(productViewModel: GetProductsViewModel, userViewModel: UserViewModel, padding: PaddingValues) {
    val userState by userViewModel.userState.collectAsState()
    val user = (userState as? UserState.Success)?.user

    LaunchedEffect(user?.uid) {
        user?.uid?.let { productViewModel.fetchProducts(it) }
    }

    // for search and filters
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("All", "Available", "Borrowed")
    var selectedFilter by remember { mutableStateOf(filters.first())}

    //for getting products from view model
    val productState by productViewModel.productState.collectAsState()


    /**
     * Column for home page with:
     * 1. Search
     * 2. Filter badges
     * 3. Grid of products
     */
    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(24.dp)
        .padding(padding)
        .fillMaxSize()

    ) {

        //=== Search Bar ===
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search items...") },
            leadingIcon = {
                          Icon(
                              imageVector = Icons.Default.Search,
                              contentDescription = "Search",
                              tint = Color.Black,
                          )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFF3EDE6)),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color(0xFFF7F7F7),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true,
            shape = RoundedCornerShape(50),
        )

        //=== Spacer ===
        Spacer(modifier = Modifier.height(12.dp))

        //=== Filter badges ===
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters.size) { i ->
                val filter = filters[i]
                FilterChip(
                    selected = (filter == selectedFilter),
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        selectedContainerColor = Color.White,
                        labelColor = Color.Black,
                        selectedLabelColor = Color.Black
                    ),
                    border = BorderStroke(
                        width = if (filter == selectedFilter) 2.dp else 1.dp,
                        color = if (filter == selectedFilter) Color.Black else Color(0xFFA2A2A2)
                    )

                )
            }
        }

        //=== Spacer ===
        Spacer(modifier = Modifier.height(16.dp))

        //=== Grid of products ===
        when (productState) {
            // when loading, show spinner
            is ProductListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // if error show text
            is ProductListState.Error -> {
                Text("Failed to load products. Please try again.")
            }

            // if success and no items, show message, else show grid of items
            is ProductListState.Success -> {
                val products = (productState as ProductListState.Success).products

                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No products to show.")
                    }
                } else {
                    // grid of cards 2x2
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(product = product)
                        }
                    }
                }


            }

            ProductListState.Idle -> {
                Text("No products to show yet")
            }
        }


    }
}

// Product Card component
@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(vertical = 8.dp)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
    ) {
        ProductCardContent(product)
    }
}

// Product Card Content component
@Composable
fun ProductCardContent(product: Product) {
    Column(modifier = Modifier.fillMaxSize()) {

        // top section image
        Box(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxWidth()
                .background(Color.White)
        ) {
            AsyncImage(
                model = product.photoUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // bottom section information
        Row(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxWidth()
                .background(Color(0xFFF7F7F7))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(product.category, style = MaterialTheme.typography.bodySmall)
                Text(product.location, style = MaterialTheme.typography.bodySmall)
            }

            // icon to show you can click to go to Details page
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go to details",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}