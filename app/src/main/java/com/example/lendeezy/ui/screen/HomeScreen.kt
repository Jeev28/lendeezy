package com.example.lendeezy.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel
import coil.compose.AsyncImage

/**
 * Shows user details currently
 * Later change to be full list of products
 */
@Composable
fun HomeScreen(viewModel: UserViewModel, padding: PaddingValues) {
    val userState by viewModel.userState.collectAsState()
    val user = (userState as? UserState.Success)?.user

    Column(modifier = Modifier
        .padding(24.dp)
        .padding(padding)) {


        /*
        Text("Welcome, ${user?.name ?: "User"}")
        Text(user?.email ?: "Email")
        AsyncImage(
            model = user?.photoUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )*/


        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.signOut() }) {
            Text("Sign Out")
        }
    }
}
