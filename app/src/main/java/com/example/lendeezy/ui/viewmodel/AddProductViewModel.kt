package com.example.lendeezy.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendeezy.data.model.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * defines states we can have when adding a product
 * Used to show toasts in UI
 */
sealed class AddProductState {
    object Idle : AddProductState()
    object Loading : AddProductState()
    object Success : AddProductState()
    data class Error(val message: String) : AddProductState()
}

/**
 * View model to add a product to Firebase
 * Handles field values, form state and image upload to Firebase Firestore
 */
class AddProductViewModel : ViewModel() {
    // text field values as states
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var category by mutableStateOf("")
    var terms by mutableStateOf("")
    var location by mutableStateOf("")
    // holds image uri
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()
    // holds state of product
    private val _productState = MutableStateFlow<AddProductState>(AddProductState.Idle)
    val productState = _productState.asStateFlow()

    // sets selected image uri
    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    //submits product to Firebase
    // Uploads image and creates product document in Firestore
    fun submitProduct(context: Context) {
        // authenticate user
        val userId = Firebase.auth.currentUser?.uid ?: return
        if (Firebase.auth.currentUser == null) {
            _productState.value = AddProductState.Error("You must be logged in to add a product")
            return
        }
        // check all fields are filled in
        if (name.isBlank() || description.isBlank() || category.isBlank() || terms.isBlank() || location.isBlank() || imageUri.value == null) {
            _productState.value = AddProductState.Error("Please fill in all fields")
            return
        }

        val imageUriLocal = imageUri.value ?: return
        val fileExtension = context.contentResolver.getType(imageUriLocal)?.split("/")?.last() ?: "jpg"
        val storageRef = Firebase.storage.reference.child("product_images/${UUID.randomUUID()}.$fileExtension")

        _productState.value = AddProductState.Loading

        viewModelScope.launch {
            try {
                // upload image to Firebase
                storageRef.putFile(imageUriLocal).await()
                val downloadUrl = storageRef.downloadUrl.await()
                // create product object
                val product = Product(
                    id = UUID.randomUUID().toString(),
                    ownerId = userId,
                    name = name,
                    category = category,
                    description = description,
                    photoUrl = downloadUrl.toString(),
                    terms = terms,
                    location = location,
                    isBorrowed = false
                )
                // upload product to Firebase
                Firebase.firestore.collection("products")
                    .document(product.id)
                    .set(product)
                    .await()

                _productState.value = AddProductState.Success
                clearForm()

            } catch (e: Exception) {
                Log.e("AddProduct", "Error: ", e)
                _productState.value = AddProductState.Error(e.message ?: "Failed to add product")
            }
        }
    }

    // Resets fields in form for after a successful submission
    private fun clearForm() {
        name = ""
        description = ""
        category = ""
        terms = ""
        location = ""
        _imageUri.value = null
    }

    // reset product state back to Idle
    fun resetState() {
        _productState.value = AddProductState.Idle
    }
}
