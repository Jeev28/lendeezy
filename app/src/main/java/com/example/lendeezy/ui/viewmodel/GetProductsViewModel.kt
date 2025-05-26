package com.example.lendeezy.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendeezy.data.model.Product
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Defines states that this view model can be in
 */
sealed class ProductListState {
    object Loading : ProductListState()
    data class Success(val products: List<Product>) : ProductListState()
    data class Error(val message: String) : ProductListState()
    object Idle : ProductListState()
}


/**
 * Gets all data from firebase for products
 * Only returns products that the current user hasn't made
 * Specifies states from ProductListState
 */
class GetProductsViewModel : ViewModel() {

    private val _state = MutableStateFlow<ProductListState>(ProductListState.Idle)
    val productState = _state.asStateFlow()

    // fetch products from firebase
    fun fetchProducts(currentUserId: String) {
        viewModelScope.launch {
            try {
                _state.value = ProductListState.Loading
                val snapshot = Firebase.firestore.collection("products").get().await()
                // all products as objects
                val allProducts = snapshot.toObjects(Product::class.java)
                // remove products the logged in user has made
                val products = allProducts.filter{ it.ownerId != currentUserId}
                _state.value = ProductListState.Success(products) // set state to success
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Failed to get products", e)
                _state.value = ProductListState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    // for Product Screen for detailed product view
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    /**
     * Gets a specific product for Product Screen by id
     */
    fun fetchProductById(productId: String) {
        viewModelScope.launch {
            try {
                _state.value = ProductListState.Loading
                val snapshot = Firebase.firestore.collection("products").document(productId).get().await()
                val product = snapshot.toObject(Product::class.java)
                if (product != null) {
                    _selectedProduct.value = product
                    _state.value = ProductListState.Success(listOf(product)) // Optional
                } else {
                    _state.value = ProductListState.Error("Product not found")
                }
            } catch (e: Exception) {
                _state.value = ProductListState.Error("Failed to load product")
            }
        }
    }
}