package com.mesaya.domain.model

enum class UserRole(val value: String, val label: String) {
    CLIENTE("cliente", "Cliente"),
    ADMIN("admin", "Administrador");

    companion object {
        fun fromValue(value: String?): UserRole =
            entries.find { it.value == value } ?: CLIENTE
    }
}
