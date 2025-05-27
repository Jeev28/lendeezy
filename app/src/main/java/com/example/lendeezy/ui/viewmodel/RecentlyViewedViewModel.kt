package com.example.lendeezy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendeezy.data.model.Product
import com.example.lendeezy.data.util.LocalDataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecentlyViewedViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = LocalDataStoreManager(application)

    private val _recentlyViewed = MutableStateFlow<List<Product>>(emptyList())
    val recentlyViewed: StateFlow<List<Product>> = _recentlyViewed

    fun loadRecentlyViewed() {
        viewModelScope.launch {
            _recentlyViewed.value = dataStore.getRecentlyViewed()
        }
    }

    fun saveProductLocally(product: Product) {
        viewModelScope.launch {
            dataStore.saveRecentlyViewed(product)
            loadRecentlyViewed()
        }
    }

}