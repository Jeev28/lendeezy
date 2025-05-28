package com.example.lendeezy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines Room table for Favourite Products
 */
@Entity(tableName = "favourite_products")
data class FavouriteProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String
)
