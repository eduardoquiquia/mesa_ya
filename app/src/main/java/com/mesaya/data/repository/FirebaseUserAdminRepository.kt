package com.mesaya.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.mesaya.domain.model.AppUser
import com.mesaya.domain.model.UserRole
import com.mesaya.domain.repository.UserAdminRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseUserAdminRepository : UserAdminRepository {
    private val bootstrapAdminEmail = "admin@mesaya.com"

    override fun getUsers(): Flow<List<AppUser>> = callbackFlow {
        val listener = FirebaseFirestore.getInstance()
            .collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents
                    ?.mapNotNull { it.toAppUserOrNull() }
                    ?.sortedBy { it.email.lowercase() }
                    .orEmpty()
                trySend(users)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateUserRole(uid: String, role: UserRole) {
        val userRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)

        val currentEmail = userRef.get().await().getString("email").orEmpty()
        if (currentEmail.equals(bootstrapAdminEmail, ignoreCase = true) && role != UserRole.ADMIN) {
            error("El administrador principal no puede cambiar a cliente.")
        }

        userRef
            .update("role", role.value)
            .await()
    }
}

private fun DocumentSnapshot.toAppUserOrNull(): AppUser? {
    val email = getString("email") ?: return null
    return AppUser(
        uid = id,
        email = email,
        role = UserRole.fromValue(getString("role"))
    )
}
