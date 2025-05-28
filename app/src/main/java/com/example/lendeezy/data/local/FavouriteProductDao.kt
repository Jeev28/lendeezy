package com.example.lendeezy.data.local

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lendeezy.data.model.FavouriteProductEntity

@Dao
interface FavouriteProductDao {

    // returns cursor for all favourite products
    @Query("SELECT * FROM favourite_products")
    fun getAllFavouritesCursor(): Cursor

    // returns cursor for single favourite product matching id
    @Query("SELECT * FROM favourite_products WHERE id = :id")
    fun getFavouriteByIdCursor(id: String): Cursor

    @Query("SELECT * FROM favourite_products")
    suspend fun getAllFavourites(): List<FavouriteProductEntity>

    @Query("SELECT * FROM favourite_products WHERE id = :id")
    suspend fun getFavouriteById(id: String): FavouriteProductEntity?

    // insert new favourite product
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(product: FavouriteProductEntity): Long

    // delete favourite product
    @Delete
    suspend fun deleteFavourite(product: FavouriteProductEntity)
}