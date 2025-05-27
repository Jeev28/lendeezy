package com.example.lendeezy.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * database for product items
 */
data class Product(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val terms: String = "",
    val location: String = "",
    val reservations: List<Reservation> = emptyList(),
)

/**
 * check if product is available or not and until when
 */
fun Product.isCurrentlyBorrowed(): Pair<Boolean, String?> {
    val today = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val currentReservation = reservations.find { reservation ->
        val start = formatter.parse(reservation.startDate)
        val end = formatter.parse(reservation.endDate)
        start != null && end != null && today >= start && today <= end
    }

    return if (currentReservation != null) {
        true to currentReservation.endDate
    } else {
        false to null
    }
}