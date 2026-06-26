package com.mesaya.domain.model

data class UserSession(
    val uid: String,
    val email: String,
    val role: UserRole
)
