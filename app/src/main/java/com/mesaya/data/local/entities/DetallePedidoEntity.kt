package com.mesaya.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detalles_pedido")
data class DetallePedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reservaId: Int,
    val mealId: String = "",
    val nombre: String = "",
    val imagenUrl: String = "",
    val precio: Double = 0.0,
    val cantidad: Int,
    val subtotal: Double,
    val platoId: Int = 0
)
