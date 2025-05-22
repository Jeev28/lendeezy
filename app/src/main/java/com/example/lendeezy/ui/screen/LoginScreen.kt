package com.example.lendeezy.ui.screen

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialException
import com.example.lendeezy.R
import com.example.lendeezy.ui.viewmodel.UserState
import com.example.lendeezy.ui.viewmodel.UserViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

/**
 * Launch Google Sign-in with Credential API
 */
@Composable
fun LoginScreen(viewModel: UserViewModel) {
    val context = LocalContext.current
    val activity = context as Activity
    val userState by viewModel.userState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // launch Google sign in
    fun launchGoogleSignIn() {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                // validate credential
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    try {
                        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleCredential.idToken

                        if (!idToken.isNullOrEmpty()) {
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            // sign in with firebase with google ID token
                            FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        viewModel.initialiseUser() // loads user into state and firestore
                                    } else {
                                        viewModel.setError("Firebase sign-in failed: ${task.exception?.message}")
                                    }
                                }
                        } else {
                            viewModel.setError("ID Token was null or empty.")
                        }
                    } catch (e: GoogleIdTokenParsingException) {
                        viewModel.setError("Credential parsing failed: ${e.message}")
                    }
                } else {
                    viewModel.setError("Unexpected credential type.")
                }
            } catch (e: GetCredentialException) {
                viewModel.setError("Credential request failed: ${e.message}")
            } catch (e: Exception) {
                viewModel.setError("Unexpected error: ${e.message}")
            }
        }
    }

    // UI for login page
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Lendeezy", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // different user states
        when (userState) {
            is UserState.Loading -> CircularProgressIndicator()
            is UserState.Error -> Text((userState as UserState.Error).message, color = Color.Red)
            else -> Button(onClick = { launchGoogleSignIn() }) {
                Text("Sign in with Google")
            }
        }
    }
}
