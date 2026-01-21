package com.example.funfriday.data.models

data class Selection(
    val userId: String = "",
    val items: List<String> = emptyList()   // List of menu item IDs
)
