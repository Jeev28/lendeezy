package com.example.lendeezy.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lendeezy.data.model.User
import com.example.lendeezy.ui.viewmodel.RecentlyViewedViewModel
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel

@Composable
fun UserScreen(navController: NavController, userViewModel: UserViewModel, recentlyViewedViewModel: RecentlyViewedViewModel, padding: PaddingValues) {

    val userState by userViewModel.userState.collectAsState()
    val user = (userState as? UserState.Success)?.user


    /**
     * Column for User page with:
     * 1. User details from Google Sign In
     * 2. Sign out
     */
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(padding)
            .fillMaxSize()
            .fillMaxWidth(),
    ) {

        // user details section
        if (user != null) {
            UserDetailsSection(user)
        }

        // sign out button
        SignOutSection(userViewModel)

        RecentlyViewedSection(navController, recentlyViewedViewModel)

    }
}


/**
 * Recently viewed grid of product cards
 */
@Composable
fun RecentlyViewedSection(navController: NavController,viewModel: RecentlyViewedViewModel) {
    // get products from local storage using view model
    LaunchedEffect(Unit) {
        viewModel.loadRecentlyViewed()
    }

    val products by viewModel.recentlyViewed.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("Recently Viewed", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        if (products.isEmpty()) {
            Text("No recently viewed products.")
        } else {
            // show grid of 2 showing a product card defined in Home Screen
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp), // Adjust as needed
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(products) { product ->
                    ProductCard(product = product, navController = navController)
                }
            }
        }
    }
}

/**
 * user details box at top of screen
 */
@Composable
fun UserDetailsSection(user: User) {
    // === User Details ===
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7))
            .border(
                width = 1.dp,
                color = Color(0xFFD9D9D9),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
    ) {

        // Row for user details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Profile image
            AsyncImage(
                model = user?.photoUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
            )

            // spacer for between text and image
            Spacer(modifier = Modifier.width(16.dp))

            // Column for LHS with name and email
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = user.name ?: "User",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user.email ?: "Email",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

        }
    }
}


/**
 * Sign out box with sign out button
 */
@Composable
fun SignOutSection(userViewModel: UserViewModel) {
    // === Sign Out ===
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(Color(0xFFF7F7F7))
            .border(
                width = 1.dp,
                color = Color(0xFFD9D9D9),
            )
    ) {

        //row for sign out text on LHS and button on RHS
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Sign out text
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Sign out button calls function from UserViewModel
            Button(
                onClick = { userViewModel.signOut() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
            ) {
                Text("Sign Out")
            }
        }
    }
}