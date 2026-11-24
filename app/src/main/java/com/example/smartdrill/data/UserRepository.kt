package com.example.smartdrill.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val username: String = "",
    val role: String = "user", // "user" or "admin"
    val createdAt: Any? = null,
    val lastReportAt: Any? = null
)

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser

    suspend fun signUp(email: String, pass: String, name: String, username: String): Result<UserProfile> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("Authentication failed")

            val profile = UserProfile(
                uid = uid,
                name = name,
                email = email,
                username = username,
                role = "user",
                createdAt = FieldValue.serverTimestamp(),
                lastReportAt = null
            )

            db.collection("users").document(uid).set(profile).await()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pass: String): Result<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("Authentication failed")

            val doc = db.collection("users").document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java) ?: UserProfile(uid = uid, email = email)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }
}
