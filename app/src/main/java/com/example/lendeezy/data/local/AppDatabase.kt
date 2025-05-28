package com.example.lendeezy.data.local

import android.content.Context
import com.example.lendeezy.data.model.FavouriteProductEntity
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * defines Room database with favourite_products table
 */
@Database(entities = [FavouriteProductEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // get DAO for favourite products
    abstract fun favouriteProductDao(): FavouriteProductDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // get database instance
        fun getInstance(context: Context): AppDatabase {
            // return existing instance or create new one
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lendeezy_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
