package com.mesaya.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlatoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("imagen_url") val imagenUrl: String
)
