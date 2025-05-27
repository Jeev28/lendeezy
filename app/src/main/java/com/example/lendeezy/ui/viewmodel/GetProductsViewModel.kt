package com.example.lendeezy.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendeezy.data.model.Product
import com.example.lendeezy.data.model.isCurrentlyBorrowed
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

    private var allProducts: List<Product> = emptyList()

    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts = _filteredProducts.asStateFlow()


    /**
     * fetch products from firebase
     */
    fun fetchProducts(currentUserId: String) {
        viewModelScope.launch {
            try {
                // set to loading
                _state.value = ProductListState.Loading
                // get all products from database except ones the current user made
                val snapshot = Firebase.firestore.collection("products").get().await()
                val fetchedProducts = snapshot.toObjects(Product::class.java)
                    .filter { it.ownerId != currentUserId }

                allProducts = fetchedProducts
                _state.value = ProductListState.Success(fetchedProducts)
                // set default to All for filters
                _filteredProducts.value = fetchedProducts
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Failed to get products", e)
                _state.value = ProductListState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * apply filters to all products
     * Search term, filter (all, available, borrowed) and categories
     */
    fun applyFilterAndSearch(query: String, selectedFilter: String, selectedCategory: String = "All") {
        val current = (_state.value as? ProductListState.Success)?.products ?: return

        val filtered = current.filter { product ->
            val matchesQuery = product.name.contains(query, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || product.category == selectedCategory
            val matchesStatus = when (selectedFilter) {
                "All" -> true
                "Available" -> !product.isCurrentlyBorrowed().first
                "Borrowed" -> product.isCurrentlyBorrowed().first
                else -> true
            }
            matchesQuery && matchesCategory && matchesStatus
        }

        _filteredProducts.value = filtered
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