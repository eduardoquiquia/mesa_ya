package com.mesaya.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "platos")
data class PlatoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val imagenUrl: String
)
