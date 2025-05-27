package com.example.lendeezy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendeezy.data.model.Product
import com.example.lendeezy.data.model.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID


/**
 * Defines states possible during making reservations
 */
sealed class ReservationState {
    object Idle : ReservationState()
    object Loading : ReservationState()
    object Success : ReservationState()
    data class Error(val message: String) : ReservationState()
}

/**
 * Reserves product in firebase
 */
class ReserveProductsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _reservationState = MutableStateFlow<ReservationState>(ReservationState.Idle)
    val reservationState: StateFlow<ReservationState> = _reservationState

    // makes reservation in firebase with user id, start and end date
    fun makeReservation(product: Product, userId: String, startDate: String, endDate: String
    ) {
        viewModelScope.launch {
            _reservationState.value = ReservationState.Loading

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = date.parse(startDate)
            val end = date.parse(endDate)

            // checks dates are valid
            if (start == null || end == null || start.after(end)) {
                _reservationState.value = ReservationState.Error("Invalid start or end date.")
                return@launch
            }

            // ensure no overlaps with other reservations
            val hasOverlap = product.reservations.any {
                val resStart = date.parse(it.startDate)
                val resEnd = date.parse(it.endDate)
                resStart != null && resEnd != null &&
                        !(end.before(resStart) || start.after(resEnd))
            }

            if (hasOverlap) {
                _reservationState.value = ReservationState.Error("Current selection overlaps with another reservation")
                return@launch
            }

            // create new reservation object
            val newReservation = Reservation(
                userId = userId,
                startDate = startDate,
                endDate = endDate,
                reservationId = UUID.randomUUID().toString()
            )
            val updatedReservations = product.reservations + newReservation

            // add reservation to reservations collection inside products collection
            db.collection("products")
                .document(product.id)
                .update("reservations", updatedReservations)
                .addOnSuccessListener {
                    _reservationState.value = ReservationState.Success
                }
                .addOnFailureListener {
                    _reservationState.value = ReservationState.Error("Failed to reserve product.")
                }
        }
    }

    fun clearReservationState() {
        _reservationState.value = ReservationState.Idle
    }
}