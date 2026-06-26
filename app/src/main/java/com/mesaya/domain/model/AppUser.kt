package com.mesaya.domain.model

data class AppUser(
    val uid: String,
    val email: String,
    val role: UserRole
)
