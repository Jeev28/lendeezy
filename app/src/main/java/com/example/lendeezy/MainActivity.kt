package com.example.lendeezy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lendeezy.ui.nav.NavGraph
import com.example.lendeezy.ui.theme.LendeezyTheme
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel
import com.google.firebase.FirebaseApp

/**
 * Launches the app and calls NavGraph to render screens
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialise firebase
        FirebaseApp.initializeApp(this)

        setContent {
            val userViewModel: UserViewModel = viewModel()
            val userState by userViewModel.userState.collectAsState()

            // find start destination based on user state
            val startDestination = when (userState) {
                is UserState.Success -> "home"
                else -> "login"
            }

            LendeezyTheme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // call navigation logic to determine what to show
                    NavGraph(startDestination = startDestination, userViewModel = userViewModel)
                }
            }
        }
    }
}