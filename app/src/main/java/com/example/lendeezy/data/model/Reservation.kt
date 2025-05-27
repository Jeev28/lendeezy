package com.example.lendeezy.data.model

import java.util.UUID

/**
 * database for reservations of individual product items
 */
data class Reservation(
    val reservationId: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val startDate: String = "",
    val endDate: String = ""
)