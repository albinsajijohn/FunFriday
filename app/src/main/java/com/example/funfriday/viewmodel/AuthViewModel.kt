package com.example.funfriday.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.funfriday.data.models.User
import com.example.funfriday.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    val user = mutableStateOf<User?>(null)
    val error = mutableStateOf<String?>(null)

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val result = repo.login(email, pass)
            if (result.isSuccess) {
                user.value = result.getOrNull()
                error.value = null
            } else {
                error.value = result.exceptionOrNull()?.localizedMessage
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            val result = repo.register(name, email, pass)
            if (result.isSuccess) {
                user.value = result.getOrNull()
                error.value = null
            } else {
                error.value = result.exceptionOrNull()?.localizedMessage
            }
        }
    }
}
