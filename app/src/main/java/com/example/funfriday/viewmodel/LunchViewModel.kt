package com.example.funfriday.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.funfriday.data.models.LunchCard
import com.example.funfriday.data.models.MenuItem
import com.example.funfriday.data.models.Selection
import com.example.funfriday.data.repository.LunchRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStreamReader

class LunchViewModel(
    private val repo: LunchRepository = LunchRepository()
) : ViewModel() {

    val cards = mutableStateListOf<LunchCard>()
    val menu = mutableStateListOf<MenuItem>()
    val selections = mutableStateListOf<Selection>()

    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    val userNameCache = mutableStateMapOf<String, String>()

    /* ---------------- USER NAME ---------------- */

    fun loadUserName(userId: String) {
        if (userNameCache.containsKey(userId)) return

        viewModelScope.launch {
            val name = repo.getUserName(userId)
            userNameCache[userId] = name
        }
    }

    /* ---------------- CREATE CARD ---------------- */

    fun createCard(title: String, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val uid = Firebase.auth.currentUser?.uid ?: return@launch
            loading.value = true

            val result = repo.createLunchCard(title, uid)

            loading.value = false

            if (result.isSuccess) onDone(result.getOrNull())
            else error.value = result.exceptionOrNull()?.message
        }
    }

    /* ---------------- ADD MENU ---------------- */

    fun addMenu(
        cardId: String,
        name: String,
        category: String,
        price: Double,
        imageUrl: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            loading.value = true

            val item = MenuItem(
                name = name,
                category = category,
                price = price,
                imageUrl = imageUrl
            )

            val result = repo.addMenuItem(cardId, item)

            loading.value = false

            if (result.isFailure) {
                error.value = result.exceptionOrNull()?.message
                return@launch
            }

            loadMenu(cardId)
            onDone()
        }
    }

    /* ---------------- LOAD MENU ---------------- */

    fun loadMenu(cardId: String) {
        viewModelScope.launch {
            val result = repo.getMenu(cardId)
            if (result.isSuccess) {
                menu.clear()
                menu.addAll(result.getOrNull()!!)
            }
        }
    }

    /* ---------------- LOAD CARDS ---------------- */

    fun loadCards() {
        viewModelScope.launch {
            val result = repo.getAllCards()
            if (result.isSuccess) {
                cards.clear()
                cards.addAll(result.getOrNull()!!)
            }
        }
    }

    /* ---------------- SAVE USER SELECTION ---------------- */

    fun saveUserSelection(cardId: String, selectedMenuIds: List<String>, onDone: () -> Unit) {
        viewModelScope.launch {
            val uid = Firebase.auth.currentUser?.uid ?: return@launch
            repo.saveSelection(cardId, Selection(uid, selectedMenuIds))
            onDone()
        }
    }

    /* ---------------- LOAD SUMMARY ---------------- */

    fun loadSelections(cardId: String) {
        viewModelScope.launch {
            val result = repo.getSelections(cardId)
            if (result.isSuccess) {
                selections.clear()
                selections.addAll(result.getOrNull()!!)
            }
        }
    }

    /* ---------------- JSON FILE UPLOAD ---------------- */

    fun uploadMenuFromJson(context: Context, cardId: String, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = InputStreamReader(inputStream)

                val type = object : TypeToken<List<MenuItem>>() {}.type
                val items: List<MenuItem> = Gson().fromJson(reader, type)

                items.forEach {
                    repo.addMenuItem(cardId, it.copy(id = ""))
                }

                loadMenu(cardId)

            } catch (e: Exception) {
                error.value = "Invalid JSON format"
            }
        }
    }

    /* ---------------- JSON TEXT UPLOAD ---------------- */

    fun uploadMenuFromJsonText(
        cardId: String,
        jsonText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (jsonText.isBlank()) {
                    onError("JSON cannot be empty")
                    return@launch
                }

                val type = object : TypeToken<List<MenuItem>>() {}.type
                val items: List<MenuItem> = Gson().fromJson(jsonText, type)

                if (items.isEmpty()) {
                    onError("No menu items found")
                    return@launch
                }

                items.forEach {
                    repo.addMenuItem(cardId, it.copy(id = ""))
                }

                loadMenu(cardId)
                onSuccess()

            } catch (e: Exception) {
                onError("Invalid JSON format")
            }
        }
    }

    /* ---------------- DELETE MENU ITEM ---------------- */

    fun deleteMenu(cardId: String, menuId: String) {
        viewModelScope.launch {
            repo.deleteMenuItem(cardId, menuId)
            loadMenu(cardId)
        }
    }

//    delete a lunchCard

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            repo.deleteCard(cardId)
            loadCards() // refresh home list
        }
    }


    /* ---------------- GET SINGLE CARD ---------------- */

    fun getCard(cardId: String, onResult: (LunchCard?) -> Unit) {
        viewModelScope.launch {
            try {
                val snap = Firebase.firestore
                    .collection("lunchCards")
                    .document(cardId)
                    .get()
                    .await()

                onResult(snap.toObject(LunchCard::class.java))
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}
