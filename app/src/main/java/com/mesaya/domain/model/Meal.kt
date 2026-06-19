package com.mesaya.domain.model

data class Meal(
    val mealId: String,
    val nombre: String,
    val categoria: String,
    val imagenUrl: String,
    val precio: Double = 18.50
)
