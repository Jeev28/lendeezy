package com.example.lendeezy.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel

@Composable
fun UserScreen(viewModel: UserViewModel, padding: PaddingValues) {

    val userState by viewModel.userState.collectAsState()
    val user = (userState as? UserState.Success)?.user


    /**
     * Column for User page with:
     * 1. User details from Google Sign In
     */
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(padding)
            .fillMaxSize()
            .padding(24.dp),
    ) {

         Text("Welcome, ${user?.name ?: "User"}")
         Text(user?.email ?: "Email")
         AsyncImage(
             model = user?.photoUrl,
             contentDescription = "Profile Picture",
             modifier = Modifier
                 .size(100.dp)
                 .clip(CircleShape)
         )
         Spacer(modifier = Modifier.height(16.dp))
         Button(onClick = { viewModel.signOut() }) {
             Text("Sign Out")
         }

    }
}
