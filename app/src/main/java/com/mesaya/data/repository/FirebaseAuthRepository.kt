package com.mesaya.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mesaya.domain.model.UserRole
import com.mesaya.domain.model.UserSession
import com.mesaya.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val context: Context
) : AuthRepository {
    private val bootstrapAdminEmail = "admin@mesaya.com"

    private val prefs by lazy {
        context.getSharedPreferences("mesaya_session", Context.MODE_PRIVATE)
    }

    override suspend fun currentSession(): UserSession? = runFirebase {
        val user = FirebaseAuth.getInstance().currentUser ?: return@runFirebase null
        val role = localRole()
            ?: if (user.email.orEmpty().equals(bootstrapAdminEmail, ignoreCase = true)) UserRole.ADMIN else null
            ?: UserRole.CLIENTE
        saveLocalRole(role)
        UserSession(
            uid = user.uid,
            email = user.email.orEmpty(),
            role = role
        )
    }

    override suspend fun signIn(email: String, password: String, role: UserRole): UserSession = runFirebase {
        val result = FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .await()
        val user = result.user ?: error("No se pudo iniciar sesion")
        val storedRole = loadRole(user.uid)
        val sessionRole = storedRole ?: if (email.trim().equals(bootstrapAdminEmail, ignoreCase = true)) {
            UserRole.ADMIN
        } else {
            role
        }
        if (storedRole == null) {
            saveRole(user.uid, email, sessionRole)
        }
        saveLocalRole(sessionRole)
        UserSession(user.uid, user.email.orEmpty(), sessionRole)
    }

    override suspend fun signUp(email: String, password: String, role: UserRole): UserSession = runFirebase {
        val finalRole = if (email.trim().equals(bootstrapAdminEmail, ignoreCase = true)) {
            UserRole.ADMIN
        } else {
            UserRole.CLIENTE
        }
        val result = FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .await()
        val user = result.user ?: error("No se pudo crear la cuenta")
        saveRole(user.uid, email, finalRole)
        saveLocalRole(finalRole)
        UserSession(user.uid, user.email.orEmpty(), finalRole)
    }

    override suspend fun resetPassword(email: String): Unit = runFirebase {
        FirebaseAuth.getInstance()
            .sendPasswordResetEmail(email)
            .await()
        Unit
    }

    override fun signOut() {
        ensureFirebase()
        FirebaseAuth.getInstance().signOut()
        prefs.edit().clear().apply()
    }

    private suspend fun loadRole(uid: String): UserRole? {
        val doc = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .await()
        return doc.getString("role")?.let { UserRole.fromValue(it) }
    }

    private suspend fun saveRole(uid: String, email: String, role: UserRole) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(
        mapOf(
            "email" to email,
            "role" to role.value
        )
            )
            .await()
    }

    private fun saveLocalRole(role: UserRole) {
        prefs.edit().putString("role", role.value).apply()
    }

    private fun localRole(): UserRole? =
        prefs.getString("role", null)?.let { UserRole.fromValue(it) }

    private fun ensureFirebase() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        if (FirebaseApp.getApps(context).isEmpty()) {
            error("Firebase no esta configurado. Agrega app/google-services.json y vuelve a sincronizar Gradle.")
        }
    }

    private suspend fun <T> runFirebase(block: suspend () -> T): T {
        try {
            ensureFirebase()
            return block()
        } catch (e: IllegalStateException) {
            throw IllegalStateException(
                e.message ?: "Firebase no esta configurado. Agrega app/google-services.json.",
                e
            )
        }
    }
}
