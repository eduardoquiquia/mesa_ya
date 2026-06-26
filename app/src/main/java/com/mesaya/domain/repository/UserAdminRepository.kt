package com.mesaya.domain.repository

import com.mesaya.domain.model.AppUser
import com.mesaya.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserAdminRepository {
    fun getUsers(): Flow<List<AppUser>>
    suspend fun updateUserRole(uid: String, role: UserRole)
}
