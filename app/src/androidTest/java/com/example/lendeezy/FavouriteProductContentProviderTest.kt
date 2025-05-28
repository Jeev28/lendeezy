package com.example.lendeezy

import android.content.ContentValues
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lendeezy.data.model.FavouriteProductEntity
import com.example.lendeezy.data.provider.FavouriteProductContentProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouriteProductContentProviderTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val contentResolver = context.contentResolver

    // sample favourite product used for tests below
    private val testProduct = FavouriteProductEntity(
        id = "test123",
        name = "Test Product",
        description = "This is a test favourite product"
    )

    // converts favourite product entity into content values
    // needed for insertion tests
    private fun createContentValues(product: FavouriteProductEntity): ContentValues {
        return ContentValues().apply {
            put("id", product.id)
            put("name", product.name)
            put("description", product.description)
        }
    }

    // remove old test data inserted
    @Before
    fun clearPreviousData() {
        val deleteUri = Uri.withAppendedPath(FavouriteProductContentProvider.CONTENT_URI, testProduct.id)
        contentResolver.delete(deleteUri, null, null)
    }

    /**
     * Test inserting product and querying it with content provider
     */
    @Test
    fun testInsertAndQuery() {
        // insert test product
        val values = createContentValues(testProduct)
        val insertUri = contentResolver.insert(FavouriteProductContentProvider.CONTENT_URI, values)

        // ensure insertion returned a valid URI
        assertNotNull("Insert URI should not be null", insertUri)
        assertEquals("Insert URI should end with the product ID", testProduct.id, insertUri?.lastPathSegment)

        // Query all products and ensure test product is there
        val cursorAll = contentResolver.query(FavouriteProductContentProvider.CONTENT_URI, null, null, null, null)
        assertNotNull(cursorAll)
        cursorAll!!.use {
            assertTrue("Cursor should have at least one entry", it.moveToFirst())
            val idIndex = it.getColumnIndex("id")
            val nameIndex = it.getColumnIndex("name")
            val descIndex = it.getColumnIndex("description")

            // ensure queried data matched inserted data for product
            assertEquals(testProduct.id, it.getString(idIndex))
            assertEquals(testProduct.name, it.getString(nameIndex))
            assertEquals(testProduct.description, it.getString(descIndex))
        }

        // Query by product ID
        val queryUri = Uri.withAppendedPath(FavouriteProductContentProvider.CONTENT_URI, testProduct.id)
        val cursorById = contentResolver.query(queryUri, null, null, null, null)
        assertNotNull(cursorById)
        cursorById!!.use {
            assertTrue(it.moveToFirst())
            val nameIndex = it.getColumnIndex("name")
            val descIndex = it.getColumnIndex("description")

            // verify returned data
            assertEquals(testProduct.name, it.getString(nameIndex))
            assertEquals(testProduct.description, it.getString(descIndex))
        }
    }

    /**
     * Test insertion with missing ID field
     */
    @Test
    fun testInsertWithMissingId() {
        val values = ContentValues().apply {
            // don't provide id
            put("name", "No ID Product")
            put("description", "Product with no id")
        }
        val insertUri = contentResolver.insert(FavouriteProductContentProvider.CONTENT_URI, values)
        // ensure the insertion fails
        assertNull("Insert with missing ID should fail and return null", insertUri)
    }


    /**
     * Test insertion with missing Name field
     */
    @Test
    fun testInsertWithMissingName() {
        val values = ContentValues().apply {
            put("id", "No Name Product")
            // don't provide name
            put("description", "Product with no name")
        }
        val insertUri = contentResolver.insert(FavouriteProductContentProvider.CONTENT_URI, values)
        // ensure the insertion fails
        assertNull("Insert with missing Name should fail and return null", insertUri)
    }


    /**
     * Test insertion with missing Description field
     */
    @Test
    fun testInsertWithMissingDescription() {
        val values = ContentValues().apply {
            put("id", "No Description Product")
            put("name", "No Description Product")
            // don't provide description
        }
        val insertUri = contentResolver.insert(FavouriteProductContentProvider.CONTENT_URI, values)
        // ensure the insertion fails
        assertNull("Insert with missing Description should fail and return null", insertUri)
    }

    /**
     * Test invalid uri
     */
    @Test(expected = IllegalArgumentException::class)
    fun testQueryWithInvalidUriThrows() {
        val invalidUri = Uri.parse("content://com.example.lendeezy.provider/invalid_uri")
        contentResolver.query(invalidUri, null, null, null, null)
    }



    /**
     * Test deleting product and ensuring it doesn't exist
     */
    @Test
    fun testDelete() {
        // Insert product first
        val values = createContentValues(testProduct)
        val insertUri = contentResolver.insert(FavouriteProductContentProvider.CONTENT_URI, values)
        assertNotNull(insertUri)

        // Delete product by ID
        val deleteUri = Uri.withAppendedPath(FavouriteProductContentProvider.CONTENT_URI, testProduct.id)
        val rowsDeleted = contentResolver.delete(deleteUri, null, null)
        // ensure 1 row deleted
        assertEquals("One row should be deleted", 1, rowsDeleted)

        // ensure querying the deleted product returns empty result
        val cursor = contentResolver.query(deleteUri, null, null, null, null)
        assertNotNull(cursor)
        cursor!!.use {
            assertEquals("Cursor should be empty after delete", 0, it.count)
        }
    }

    /**
     * Test deleting product with ID that doesn't exist
     */
    @Test
    fun testDeleteNonExistingIdReturnsZero() {
        val deleteUri = Uri.withAppendedPath(FavouriteProductContentProvider.CONTENT_URI, "nonexistentid")
        val rowsDeleted = contentResolver.delete(deleteUri, null, null)
        assertEquals("Deleting non-existent ID product should delete 0 rows", 0, rowsDeleted)
    }

}
