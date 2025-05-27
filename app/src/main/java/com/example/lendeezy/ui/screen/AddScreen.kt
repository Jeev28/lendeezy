package com.example.lendeezy.ui.screen


import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.lendeezy.ui.viewmodel.AddProductState
import com.example.lendeezy.ui.viewmodel.AddProductViewModel
import com.example.lendeezy.ui.viewmodel.UserViewModel
import android.Manifest
import android.location.Geocoder
import android.location.Location
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Form for a user to create a new product to lend
 */
@Composable
fun AddScreen(viewModel: AddProductViewModel, padding: PaddingValues) {
    val context = LocalContext.current
    val imageUri by viewModel.imageUri.collectAsState()
    val productState by viewModel.productState.collectAsState()

    // showing toasts
    LaunchedEffect(productState) {
        when (val state = productState) {
            is AddProductState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is AddProductState.Success -> {
                Toast.makeText(context, "New Product added", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    // getting images from gallery
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setImageUri(uri)
    }

    // UI for form
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // heading with underline
            HeaderSection()
            // image picker and shows image icon
            ImagePickerSection(imageUri = imageUri, onPickImage = { launcher.launch("image/*") }, onRemoveImage = { viewModel.setImageUri(null) })
            // form UI
            ProductFormFields(
                name = viewModel.name,
                onNameChange = { viewModel.name = it },
                description = viewModel.description,
                onDescriptionChange = { viewModel.description = it },
                category = viewModel.category,
                onCategoryChange = { viewModel.category = it },
                terms = viewModel.terms,
                onTermsChange = { viewModel.terms = it },
                location = viewModel.location,
                onLocationChange = { viewModel.location = it }
            )
            // location UI
            LocationFormField(location = viewModel.location, onLocationChange = { viewModel.location = it })
            SubmitButton(onClick = { viewModel.submitProduct(context) })
        }

        // show loading icon
        if (productState is AddProductState.Loading) {
            LoadingOverlay()
        }
    }
}

/**
 * Shows title for the page and a underline
 */
@Composable
fun HeaderSection() {
    Text(
        text = "Lend an item",
        color = Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
    HorizontalDivider(
        modifier = Modifier.padding(top = 4.dp),
        thickness = 1.dp,
        color = Color.Gray
    )
}

/**
 * Shows button to pick an image which opens the gallery
 * Shows icon when image selected and allows user to remove it
 */
@Composable
fun ImagePickerSection(imageUri: Uri?, onPickImage: () -> Unit, onRemoveImage: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        // pick an image when you click button
        OutlinedButton(
            onClick = onPickImage,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Icon",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Photo", color = Color.Black)
        }

        // If image picked, show thumbnail
        imageUri?.let { uri ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0F0F0))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // show small thumbnail of image
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Thumbnail Image",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Image Added",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                // remove image button
                IconButton(onClick = onRemoveImage) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Image",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Form fields for name, description, category, terms, location
 */
@Composable
fun ProductFormFields(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    terms: String,
    onTermsChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit
) {
    LendFormTextBox(value = name, onValueChange = onNameChange, label = "Product Name", modifier = Modifier.fillMaxWidth())
    LendFormTextBox(value = description, onValueChange = onDescriptionChange, label = "Description", modifier = Modifier.fillMaxWidth())
    LendFormTextBox(value = category, onValueChange = onCategoryChange, label = "Category", modifier = Modifier.fillMaxWidth())
    LendFormTextBox(value = terms, onValueChange = onTermsChange, label = "Borrowing Terms", modifier = Modifier.fillMaxWidth())
    //LendFormTextBox(value = location, onValueChange = onLocationChange, label = "Location", modifier = Modifier.fillMaxWidth())

}


@Composable
fun LocationFormField(
    location: String,
    onLocationChange: (String) -> Unit,
) {
    val context = LocalContext.current
    // remember for getting device location
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()

    // Column with text box for manual input and button
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        LendFormTextBox(
            value = location,
            onValueChange = onLocationChange,
            label = "Location",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // on click, gets device current location
        OutlinedButton(onClick = {
            coroutineScope.launch {
                // ensure app has location permissions granted
                val permissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PermissionChecker.PERMISSION_GRANTED

                // if permission not granted show error
                if (!permissionGranted) {
                    Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                try {
                    // cancellation token used if request is cancelled
                    val cancellationToken = com.google.android.gms.tasks.CancellationTokenSource()

                    // get device current location
                    fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationToken.token
                    ).addOnSuccessListener { loc: Location? ->
                        if (loc != null) {
                            // use geocoder to convert lat and long into actual address
                            val geocoder = Geocoder(context)
                            // get one matching address
                            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                val addressString = listOfNotNull(
                                    // street name
                                    address.thoroughfare,
                                    // city
                                    address.locality,
                                    // region
                                    address.adminArea,
                                    // post code
                                    address.postalCode
                                ).joinToString(", ")
                                onLocationChange(addressString)
                            } else {
                                Toast.makeText(context, "Could not get current address", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error getting location. Try again later.", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("Use Current Location")
        }
    }
}


/**
 * full width button to submit
 * On click, this calls the firebase storage from view model
 */
@Composable
fun SubmitButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFFDC8255),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            "Add Product",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * When submit button clicked, show a loading icon over screen
 */
@Composable
fun LoadingOverlay() {
    // greyed our box in background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        // loading icon
        CircularProgressIndicator(color = Color.White)
    }
}

/**
 * custom text box styling for form fields
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LendFormTextBox(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black,
            focusedLabelColor = Color.Black
        )
    )
}