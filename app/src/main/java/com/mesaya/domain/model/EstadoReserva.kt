package com.mesaya.domain.model

enum class EstadoReserva(val value: String, val label: String) {
    PENDIENTE("pendiente", "Pendiente"),
    EN_PREPARACION("en_preparacion", "En preparacion"),
    COMPLETADA("completada", "Completada");

    companion object {
        fun fromValue(value: String): EstadoReserva = when (value) {
            "confirmada", "lista_servir", "atendida", "cancelada" -> COMPLETADA
            else -> entries.find { it.value == value } ?: PENDIENTE
        }
    }
}
