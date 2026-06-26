package com.mesaya.viewmodel

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mesaya.domain.model.EstadoReserva
import com.mesaya.domain.model.UserRole
import com.mesaya.domain.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class PerfilAccount(
    val email: String = "",
    val role: UserRole = UserRole.CLIENTE,
    val photoUri: String? = null,
    val alertsEnabled: Boolean = true
)

data class PerfilStats(
    val totalReservas: Int = 0,
    val reservasEnPreparacion: Int = 0,
    val reservasCompletadas: Int = 0
)

class PerfilViewModel(
    private val context: Context,
    private val repository: ReservaRepository
) : ViewModel() {
    private val prefs by lazy {
        context.getSharedPreferences("mesaya_session", Context.MODE_PRIVATE)
    }

    private val _stats = MutableStateFlow(PerfilStats())
    val stats: StateFlow<PerfilStats> = _stats

    private val _account = MutableStateFlow(loadAccount())
    val account: StateFlow<PerfilAccount> = _account

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getReservas()
                .catch { /* silencioso */ }
                .collect { reservas ->
                    _stats.value = PerfilStats(
                        totalReservas = reservas.size,
                        reservasEnPreparacion = reservas.count {
                            EstadoReserva.fromValue(it.estado) == EstadoReserva.EN_PREPARACION
                        },
                        reservasCompletadas = reservas.count {
                            EstadoReserva.fromValue(it.estado) == EstadoReserva.COMPLETADA
                        }
                    )
                }
        }
    }

    fun updatePhoto(uri: Uri?) {
        val photo = uri?.toString()
        prefs.edit().putString("profile_photo_uri", photo).apply()
        _account.value = _account.value.copy(photoUri = photo)
    }

    fun clearPhoto() {
        prefs.edit().remove("profile_photo_uri").apply()
        _account.value = _account.value.copy(photoUri = null)
    }

    fun setAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("alerts_enabled", enabled).apply()
        _account.value = _account.value.copy(alertsEnabled = enabled)
    }

    private fun loadAccount(): PerfilAccount {
        val role = prefs.getString("role", null)?.let { UserRole.fromValue(it) } ?: UserRole.CLIENTE
        return PerfilAccount(
            email = FirebaseAuth.getInstance().currentUser?.email.orEmpty(),
            role = role,
            photoUri = prefs.getString("profile_photo_uri", null),
            alertsEnabled = prefs.getBoolean("alerts_enabled", true)
        )
    }

    companion object {
        fun factory(context: Context, repository: ReservaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PerfilViewModel(context.applicationContext, repository) as T
            }
    }
}
