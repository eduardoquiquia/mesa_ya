package com.mesaya.domain.model

enum class EstadoReserva(val value: String, val label: String) {
    PENDIENTE("pendiente", "Pendiente"),
    CONFIRMADA("confirmada", "Confirmada"),
    EN_PREPARACION("en_preparacion", "En preparación"),
    LISTA_SERVIR("lista_servir", "Lista para servir"),
    ATENDIDA("atendida", "Atendida"),
    CANCELADA("cancelada", "Cancelada");

    companion object {
        fun fromValue(value: String): EstadoReserva =
            entries.find { it.value == value } ?: PENDIENTE
    }
}
