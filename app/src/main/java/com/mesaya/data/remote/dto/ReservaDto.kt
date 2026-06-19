package com.mesaya.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReservaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("cliente_nombre") val clienteNombre: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("numero_personas") val numeroPersonas: Int,
    @SerializedName("mesa_id") val mesaId: Int,
    @SerializedName("estado") val estado: String
)
