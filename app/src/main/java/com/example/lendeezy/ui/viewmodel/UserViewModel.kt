package com.example.lendeezy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendeezy.data.model.User
import com.example.lendeezy.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel manages UI related data
 * viewModelScope is a coroutine scope which avoid memory leaks
 */

/**
 * Defines possible UI state during login
 */
sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    data class Success(val user: User) : UserState()
    data class Error(val message: String) : UserState()
}

/**
 * Handles logic between UI and repository
 */
class UserViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    // mutable state only editable inside view model
    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    // public read-only version
    val userState: StateFlow<UserState> = _userState

    // check or create user on login
    // sets state to loading, then success or error
    fun initialiseUser() {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val firebaseUser = userRepository.getCurrentUser()
                if (firebaseUser != null) {
                    // only writes to Firestore if they don't already exist
                    userRepository.createNewUser(firebaseUser)
                    _userState.value = UserState.Success(firebaseUser)
                } else {
                    _userState.value = UserState.Error("No logged-in user")
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

    // log user out and set state back to Idle
    fun signOut() {
        userRepository.signOut()
        _userState.value = UserState.Idle
    }
}