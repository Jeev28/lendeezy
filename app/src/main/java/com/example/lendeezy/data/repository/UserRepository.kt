package com.example.lendeezy.data.repository

import com.example.lendeezy.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
/**
 * Handles all user-related firestore logic
 */
class UserRepository(
    // init firebase auth and firestore instances
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    init {
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
    }

    // check if user document exists, else makes a new one in users collection
    suspend fun createNewUser(user: User) {
        val docRef = db.collection("users").document(user.uid)
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            docRef.set(user).await()
        }
    }

    // get user by ID and convert into User object
    suspend fun getUserById(uid: String): User? {
        val doc = db.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java)
    }

    // sign current user out of firebase
    fun signOut() {
        auth.signOut()
    }

    // return uid of currently signed in user
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // return email of currently signed in user
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    // converts firebase user object into User data model
    fun getCurrentUser(): User? {
        val user = auth.currentUser
        return if (user != null) {
            User(
                uid = user.uid,
                name = user.displayName ?: "",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: ""
            )
        } else null
    }
}