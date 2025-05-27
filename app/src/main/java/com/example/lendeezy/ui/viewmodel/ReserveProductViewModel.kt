package com.example.lendeezy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lendeezy.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ReserveProductViewModelFactory(
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReserveProductViewModel::class.java)) {
            return ReserveProductViewModel(productRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ReserveProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _reservationState = MutableStateFlow<String?>(null)
    val reservationResult = _reservationState.asStateFlow()

    fun addReservation(productId: String, userId: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            try {
                productRepository.addReservation(productId, userId, startDate, endDate)
                _reservationState.value = "Reservation added"
            } catch (e: Exception) {
                _reservationState.value = e.message ?: "Something went wrong"
            }
        }
    }

    fun clearReservationResult() {
        _reservationState.value = null
    }

}