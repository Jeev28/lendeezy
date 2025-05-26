package com.example.lendeezy.ui.screen

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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

    // show toast if error
    if (userState is UserState.Error) {
        LaunchedEffect(userState) {
            Toast.makeText(context, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
            Log.e("LoginScreen", "Sign-in error: ${(userState as UserState.Error).message}")
        }
    }

    // launch Google sign in
    fun launchGoogleSignIn() {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
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

    // UI for login screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // grid of images
        LoginImageGrid()

        // title, slogan, sign in button
        LoginFooter(
            userState = userState,
            onGoogleSignInClick = { launchGoogleSignIn() }
        )
    }

}


/**
 * Grid of images
 * Grid of 2, then 3, then 2
 */
@Composable
fun LoginImageGrid() {
    // images for grid
    val images = listOf(
        R.drawable.image_4,
        R.drawable.image_6,
        R.drawable.image_8,
        R.drawable.image_9,
        R.drawable.image_2,
        R.drawable.image_10,
        R.drawable.image_3,
    )

    // hardcodes images from above into each row into each grid item product
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // row 1 with 2 items
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoginGridProduct(imageRes = images[0], modifier = Modifier.weight(1f))
            LoginGridProduct(imageRes = images[1], modifier = Modifier.weight(1f))
        }
        // row 2 with 3 items
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoginGridProduct(imageRes = images[2], modifier = Modifier.weight(1f))
            LoginGridProduct(imageRes = images[3], modifier = Modifier.weight(1f))
            LoginGridProduct(imageRes = images[4], modifier = Modifier.weight(1f))
        }
        // row 3 with 2 items
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoginGridProduct(imageRes = images[5], modifier = Modifier.weight(1f))
            LoginGridProduct(imageRes = images[6], modifier = Modifier.weight(1f))
        }
    }
}


/**
 * Each individual grid item
 * Takes in a modifier used for weight modifier as it can
 * only be applied inside a Row or Column, not Box
 * and takes in image
 */
@Composable
fun LoginGridProduct(
    imageRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Lendeezy Example Images",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}


/**
 * Title, Slogan, Login button
 * Takes in state for errors and loading, and sign in onclick event
 */
@Composable
fun LoginFooter(
    userState: UserState,
    onGoogleSignInClick: () -> Unit
) {
    // box adds gray background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFD9D9D9),
                shape = RoundedCornerShape(topStart = 80.dp, topEnd = 80.dp)
            ),
    ) {
        // column with title, slogan and button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // title
            Text(
                text = "Lendeezy",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // slogan
            Text(
                text = "Lend Anything with Ease",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.DarkGray,
                    fontStyle = FontStyle.Italic
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign in with google button
            if (userState is UserState.Loading) {
                CircularProgressIndicator()
            } else {
                Image(
                    painter = painterResource(id = R.drawable.google_sign_in),
                    contentDescription = "Sign in with Google",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { onGoogleSignInClick() }
                )
            }
        }
    }
}


