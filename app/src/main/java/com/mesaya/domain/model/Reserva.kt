package com.mesaya.domain.model

import java.util.Date

data class Reserva(
    val id: Int = 0,
    val userId: String = "",
    val clienteNombre: String,
    val fecha: Date,
    val hora: String = "",
    val numeroPersonas: Int,
    val mesaId: Int,
    val estado: String = EstadoReserva.PENDIENTE.value,
    val avisoLlegada: Boolean = false,
    val total: Double = 0.0,
    val metodoPago: String = MetodoPago.PENDIENTE.value
)
