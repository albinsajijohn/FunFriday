package com.example.funfriday.data.models

data class Selection(
    val userId: String = "",
    val items: Map<String, Int> = emptyMap() // menuId -> quantity
)

