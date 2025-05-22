package com.example.lendeezy.data.model

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
    val isBorrowed: Boolean = false,
    val borrowedUntil: String? = null,
    val reservations: List<Reservation> = emptyList(),
)