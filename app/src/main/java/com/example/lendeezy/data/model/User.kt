package com.example.lendeezy.data.model

/**
 * Represents a single document in the users collection in firestore
 * The fields match Google Sign-in fields
**/
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
)