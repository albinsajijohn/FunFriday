package com.example.funfriday.data.models

import com.google.firebase.Timestamp

data class LunchCard(
    val id: String = "",
    val title: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp? = null
)
