package com.example.lendeezy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ProductRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun addReservation(productId: String, userId: String, startDate: String, endDate: String) {
        val start = formatter.parse(startDate)
        val end = formatter.parse(endDate)

        if (start == null || end == null || !end.after(start)) {
            throw IllegalArgumentException("End date must be after start date")
        }

        val productRef = firestore.collection("products").document(productId)
        val snapshot = productRef.get().await()

        val existingReservations = snapshot.get("reservations") as? List<Map<*, *>> ?: emptyList()

        val hasOverlap = existingReservations.any {
            val existingStart = it["startDate"]?.toString()?.let { formatter.parse(it) }
            val existingEnd = it["endDate"]?.toString()?.let { formatter.parse(it) }
            existingStart != null && existingEnd != null &&
                    !(end.before(existingStart) || start.after(existingEnd))
        }

        if (hasOverlap) {
            throw IllegalStateException("Selected dates overlap with an existing reservation")
        }

        val newReservation = mapOf(
            "userId" to userId,
            "startDate" to startDate,
            "endDate" to endDate
        )

        firestore.runTransaction { transaction ->
            val current = transaction.get(productRef)
            val updatedReservations = (current.get("reservations") as? List<Map<*, *>>)?.toMutableList()
                ?: mutableListOf()

            updatedReservations.add(newReservation)

            transaction.update(productRef, mapOf(
                "reservations" to updatedReservations,
                "isBorrowed" to true,
                "borrowedUntil" to endDate
            ))
        }.await()
    }
}