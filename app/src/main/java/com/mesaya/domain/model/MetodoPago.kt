package com.mesaya.domain.model

enum class MetodoPago(val value: String, val label: String) {
    PENDIENTE("pendiente", "Pendiente"),
    YAPE("yape", "Yape"),
    PLIN("plin", "Plin");

    companion object {
        val onlineMethods = listOf(YAPE, PLIN)

        fun fromValue(value: String?): MetodoPago =
            entries.find { it.value == value } ?: PENDIENTE
    }
}
