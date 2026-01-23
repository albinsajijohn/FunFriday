package com.example.funfriday.data.repository

import com.example.funfriday.data.models.LunchCard
import com.example.funfriday.data.models.MenuItem
import com.example.funfriday.data.models.Selection
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class LunchRepository {

    private val db = Firebase.firestore

    /* ---------------- CREATE LUNCH CARD ---------------- */

    suspend fun createLunchCard(
        title: String,
        createdBy: String
    ): Result<String> {
        return try {
            val doc = db.collection("lunchCards").document()

            val card = LunchCard(
                id = doc.id,
                title = title,
                createdBy = createdBy,
                createdAt = Timestamp.now()
            )

            doc.set(card).await()
            Result.success(doc.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /* ---------------- ADD MENU ITEM ---------------- */

    suspend fun addMenuItem(
        cardId: String,
        item: MenuItem
    ): Result<String> {
        return try {
            val doc = db.collection("lunchCards")
                .document(cardId)
                .collection("menus")   // ✅ CORRECT
                .document()

            val finalItem = item.copy(id = doc.id)
            doc.set(finalItem).await()

            Result.success(doc.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /* ---------------- LOAD MENU ---------------- */

    suspend fun getMenu(cardId: String): Result<List<MenuItem>> {
        return try {
            val snap = db.collection("lunchCards")
                .document(cardId)
                .collection("menus")   // ✅ CORRECT
                .get()
                .await()

            Result.success(snap.toObjects(MenuItem::class.java))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /* ---------------- DELETE MENU ITEM ---------------- */

    suspend fun deleteMenuItem(
        cardId: String,
        menuId: String
    ): Result<Unit> {
        return try {
            db.collection("lunchCards")
                .document(cardId)
                .collection("menus")   // ✅ FIXED HERE
                .document(menuId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /* ---------------- USER SELECTION ---------------- */

    suspend fun saveSelection(
        cardId: String,
        selection: Selection
    ): Result<Unit> {
        return try {
            db.collection("lunchCards")
                .document(cardId)
                .collection("selections")
                .document(selection.userId)
                .set(selection)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSelections(cardId: String): Result<List<Selection>> {
        return try {
            val snap = db.collection("lunchCards")
                .document(cardId)
                .collection("selections")
                .get()
                .await()

            Result.success(snap.toObjects(Selection::class.java))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /* ---------------- USERS ---------------- */

    suspend fun getUserName(userId: String): String {
        return try {
            val snap = db.collection("users")
                .document(userId)
                .get()
                .await()

            snap.getString("name") ?: "Unknown User"

        } catch (e: Exception) {
            "Unknown User"
        }
    }

    /* ---------------- LOAD ALL CARDS ---------------- */

    suspend fun getAllCards(): Result<List<LunchCard>> {
        return try {
            val snap = db.collection("lunchCards").get().await()
            Result.success(snap.toObjects(LunchCard::class.java))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


//delete a created lunchCard

    suspend fun deleteCard(cardId: String): Result<Unit> {
        return try {
            Firebase.firestore
                .collection("lunchCards")
                .document(cardId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
