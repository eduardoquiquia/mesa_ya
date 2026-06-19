package com.mesaya.domain.model

data class DetallePedido(
    val id: Int = 0,
    val reservaId: Int,
    val mealId: String = "",
    val nombre: String = "",
    val imagenUrl: String = "",
    val precio: Double = 0.0,
    val cantidad: Int,
    val subtotal: Double,
    val platoId: Int = 0
)
