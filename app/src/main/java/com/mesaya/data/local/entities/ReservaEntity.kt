package com.mesaya.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservas")
data class ReservaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val clienteNombre: String,
    val fecha: Long,
    val hora: String = "",
    val numeroPersonas: Int,
    val mesaId: Int,
    val estado: String = "pendiente",
    val avisoLlegada: Boolean = false,
    val total: Double = 0.0,
    val metodoPago: String = "pendiente"
)
