package com.example.lendeezy.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lendeezy.data.model.Product
import com.example.lendeezy.ui.viewmodel.GetProductsViewModel
import com.example.lendeezy.ui.viewmodel.ProductListState
import com.example.lendeezy.ui.viewmodel.SellerUserState
import com.example.lendeezy.ui.viewmodel.SellerViewModel
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
            .verticalScroll(rememberScrollState()),
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
                selectedProduct?.let { product ->
                    ProductDetailContent(product = product)
                } ?: Text("Product not found.")
            }
            ProductListState.Idle -> {
                Text("No product data yet.")
            }
        }
    }

}

/**
 * Content of Product Screen with
 * 1. seller info
 * 2. Title and category
 * 3. Description, Terms, and Location
 * 4. Rent or borrow status
 */
@Composable
fun ProductDetailContent(product: Product) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // owner info, product name + category, intent to Mail
        ProductDetailTop(product)

        HorizontalDivider(thickness = 26.dp, color = Color(0xFFFF3F3F3))

        // description, terms, location, intent to Maps
        ProductDetailMiddle(product)

        HorizontalDivider(thickness = 26.dp, color = Color(0xFFFF3F3F3))

        // borrow status with rent button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BorrowStatus(isBorrowed = product.isBorrowed, borrowedUntil = product.borrowedUntil)
        }

    }
}

/**
 * Image, seller details, title and category
 */
@Composable
fun ProductDetailTop(product: Product) {
    AsyncImage(
        model = product.photoUrl,
        contentDescription = product.name,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentScale = ContentScale.Crop
    )

    // owner information
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        OwnerInfo(product, ownerId = product.ownerId)
    }

    HorizontalDivider(thickness = 1.dp, color = Color.Gray)

    // product name and category
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(product.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text("Category: ${product.category}", style = MaterialTheme.typography.bodyLarge)
    }

}

/**
 * Description, terms and location
 * Button to open Google Maps for location
 */
@Composable
fun ProductDetailMiddle(product: Product) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // description
        Text("Description", style = MaterialTheme.typography.titleMedium)
        Text(product.description, style = MaterialTheme.typography.bodyLarge)

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        // terms
        Text("Terms", style = MaterialTheme.typography.titleMedium)
        Text(product.terms, style = MaterialTheme.typography.bodyLarge)

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        // button to view on Google Maps
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column() {
                Text("Location", style = MaterialTheme.typography.titleMedium)
                Text(product.location, style = MaterialTheme.typography.bodyLarge)
            }

            // button which on click, opens Maps for that location
            Button(
                onClick = {
                    // create URI
                    val mapsIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(product.location)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, mapsIntentUri)
                    // use the Google Maps app
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text("View")
            }
        }
    }
}


/**
 * If rented, show message it is rented, else show Rent button
 */
@Composable
fun BorrowStatus(isBorrowed: Boolean, borrowedUntil: String?) {
    // if borrowed, show message
    if (isBorrowed) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Borrowed until: $borrowedUntil")
        }
    } else {
        // if available, show Rent button
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Available for rent")
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("Rent")
            }
        }

    }
}

/**
 * Show seller information
 */
@Composable
fun OwnerInfo(product: Product ,ownerId: String, sellerViewModel: SellerViewModel = viewModel()) {
    val sellerState by sellerViewModel.sellerState.collectAsState()
    val context = LocalContext.current

    // fetch seller details
    LaunchedEffect(ownerId) {
        sellerViewModel.fetchUser(ownerId)
    }

    when (sellerState) {
        // when loading, show spinner
        is SellerUserState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        is SellerUserState.Error -> {
            Text("Could not load owner", color = Color.Red)
        }

        // if success show image and name of seller
        is SellerUserState.Success -> {
            val owner = (sellerState as SellerUserState.Success).user

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // image and name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = owner.photoUrl,
                        contentDescription = owner.name,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(owner.name, style = MaterialTheme.typography.bodyLarge)
                }

                // button which opens Mail app to email seller
                Button(
                    onClick = {
                        // intent to open Mail
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${owner.email}")
                            putExtra(Intent.EXTRA_SUBJECT, "More info about your product ${product.name} on Lendeezy")
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Email")
                }
            }
        }

        SellerUserState.Idle -> {
            // nothing
        }
    }
}
