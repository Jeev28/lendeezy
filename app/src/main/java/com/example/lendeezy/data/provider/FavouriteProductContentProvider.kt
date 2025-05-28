package com.example.lendeezy.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.lendeezy.data.local.AppDatabase
import com.example.lendeezy.data.model.FavouriteProductEntity
import kotlinx.coroutines.runBlocking

/**
 * Exposes FavouriteProduct data for other apps
 */
class FavouriteProductContentProvider : ContentProvider()  {

    companion object {
        // unique id for content provider
        const val AUTHORITY = "com.example.lendeezy.provider"
        // base content uri to access favourite products table
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/favourite_products")

        // 1 - code for uri to access all favourites
        // 2 - code for uri to access a single favourite
        private const val FAVORITES = 1
        private const val FAVORITE_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "favourite_products", FAVORITES)
            addURI(AUTHORITY, "favourite_products/*", FAVORITE_ID) // /favourite_products/id
        }
    }

    // reference to room db
    private lateinit var database: AppDatabase

    //initialise database instance
    override fun onCreate(): Boolean {
        context?.let {
            database = AppDatabase.getInstance(it)
            return true
        }
        return false
    }

    // handle query requests
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        // check incoming uri to see which query to do
        val cursor = when (uriMatcher.match(uri)) {
            // query all products
            FAVORITES -> database.favouriteProductDao().getAllFavouritesCursor()
            // query a single product
            FAVORITE_ID -> {
                val id = uri.lastPathSegment ?: throw IllegalArgumentException("Invalid ID")
                database.favouriteProductDao().getFavouriteByIdCursor(id)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        cursor?.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    // Handles insert requests
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (uriMatcher.match(uri) != FAVORITES) {
            throw IllegalArgumentException("Invalid URI for insert: $uri")
        }
        values ?: return null

        val id = values.getAsString("id")
        val name = values.getAsString("name")
        val description = values.getAsString("description")

        // check fields is not null
        if (id.isNullOrEmpty() || name.isNullOrEmpty() || description.isNullOrEmpty()) {
            return null
        }

        // convert content values into FavouriteProductEntity object
        val product = FavouriteProductEntity(id, name ?: "", description ?: "")


        // insert into database
        runBlocking {
            database.favouriteProductDao().insertFavourite(product)
        }

        // Build URI with string ID added
        val newUri = CONTENT_URI.buildUpon().appendPath(product.id).build()

        context?.contentResolver?.notifyChange(newUri, null)
        return newUri // return uri of new data
    }

    // Handle delete requests
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return runBlocking {
            when (uriMatcher.match(uri)) {
                FAVORITE_ID -> {
                    // get id
                    val id = uri.lastPathSegment ?: return@runBlocking 0
                    // fetch matiching product from database
                    val product = database.favouriteProductDao().getFavouriteById(id)
                    product?.let {
                        // delete from database
                        database.favouriteProductDao().deleteFavourite(it)
                        context?.contentResolver?.notifyChange(uri, null)
                        1 // 1 row deleted
                    } ?: 0 // 0 rows deleted if product not found
                }
                else -> throw IllegalArgumentException("Invalid URI for delete: $uri")
            }
        }
    }

    // Nothing to update so error thrown
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Update not supported")
    }

    // return type for data of given uri
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            FAVORITES -> "vnd.android.cursor.dir/$AUTHORITY.favorite_products"
            FAVORITE_ID -> "vnd.android.cursor.item/$AUTHORITY.favorite_products"
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
    }

}