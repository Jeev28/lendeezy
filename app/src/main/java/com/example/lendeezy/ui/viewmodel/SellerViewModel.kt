package com.example.lendeezy.ui.viewmodel

import com.example.lendeezy.data.model.User
import com.example.lendeezy.data.repository.UserRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SellerUserState {
    object Loading : SellerUserState()
    data class Success(val user: User) : SellerUserState()
    data class Error(val message: String) : SellerUserState()
    object Idle : SellerUserState()
}

/**
 * Gets user object by ID using User Repository function and sets proper state
 */
class SellerViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _sellerState = MutableStateFlow<SellerUserState>(SellerUserState.Idle)
    val sellerState: StateFlow<SellerUserState> = _sellerState

    fun fetchUser(uid: String) {
        viewModelScope.launch {
            _sellerState.value = SellerUserState.Loading
            try {
                val user = userRepository.getUserById(uid)
                if (user != null) {
                    _sellerState.value = SellerUserState.Success(user)
                } else {
                    _sellerState.value = SellerUserState.Error("User not found")
                }
            } catch (e: Exception) {
                _sellerState.value = SellerUserState.Error("Error fetching user")
            }
        }
    }

}