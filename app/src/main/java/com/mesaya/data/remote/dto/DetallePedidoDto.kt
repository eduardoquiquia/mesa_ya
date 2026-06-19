package com.mesaya.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DetallePedidoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("reserva_id") val reservaId: Int,
    @SerializedName("plato_id") val platoId: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("subtotal") val subtotal: Double
)
