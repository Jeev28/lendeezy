package com.example.lendeezy.data.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lendeezy.data.model.Product
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.flow.first


private val Context.dataStore by preferencesDataStore(name = "recently_viewed")

class LocalDataStoreManager(private val context: Context) {

    private val gson = Gson()
    private val recentlyViewedKey = stringPreferencesKey("recently_viewed_list")

    suspend fun saveRecentlyViewed(product: Product) {
        val currentList = getRecentlyViewed().toMutableList()
        currentList.removeAll { it.id == product.id}
        currentList.add(0, product)
        if (currentList.size > 12) currentList.removeLast()

        val jsonList = gson.toJson(currentList)
        context.dataStore.edit { prefs ->
        prefs[recentlyViewedKey] = jsonList}
    }

    suspend fun getRecentlyViewed(): List<Product> {
        val prefs = context.dataStore.data.first()
        val json = prefs[recentlyViewedKey] ?: return emptyList()
        val type = object : TypeToken<List<Product>>() {}.type
        return gson.fromJson(json, type)
    }

    suspend fun clearRecentlyViewed() {
        context.dataStore.edit {
            it.remove(recentlyViewedKey)
        }
    }


}