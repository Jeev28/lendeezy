package com.example.lendeezy.data.model

/**
 * database for reservations of individual product items
 */
data class Reservation(
    val userId: String = "",
    val startDate: String = "",
    val endDate: String = ""
)