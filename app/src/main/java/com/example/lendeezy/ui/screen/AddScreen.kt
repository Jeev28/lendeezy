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
    LendFormTextBox(value = location, onValueChange = onLocationChange, label = "Location", modifier = Modifier.fillMaxWidth())
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