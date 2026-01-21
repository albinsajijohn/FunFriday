package com.example.funfriday.data.repository

import com.example.funfriday.data.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    // Register new user
    suspend fun register(name: String, email: String, pass: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user!!.uid

            val user = User(uid, name, email)
            db.collection("users").document(uid).set(user).await()

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login existing user
    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user!!.uid

            val doc = db.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java)
                ?: throw Exception("User profile not found")

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
