package com.mesaya.domain.repository

import com.mesaya.domain.model.UserRole
import com.mesaya.domain.model.UserSession

interface AuthRepository {
    suspend fun currentSession(): UserSession?
    suspend fun signIn(email: String, password: String, role: UserRole): UserSession
    suspend fun signUp(email: String, password: String, role: UserRole): UserSession
    suspend fun resetPassword(email: String)
    fun signOut()
}
