package com.mesaya.domain.model

data class Plato(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val imagenUrl: String
)
