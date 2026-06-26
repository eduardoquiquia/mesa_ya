package com.mesaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mesaya.domain.model.UserRole
import com.mesaya.domain.model.UserSession
import com.mesaya.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

data class AuthUiState(
    val loading: Boolean = false,
    val checkingSession: Boolean = false,
    val session: UserSession? = null,
    val error: String? = null,
    val message: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true, checkingSession = true)
            try {
                val session = withTimeout(3500) {
                    withContext(Dispatchers.IO) {
                        repository.currentSession()
                    }
                }
                _uiState.value = if (session != null) {
                    AuthUiState(session = session, checkingSession = false)
                } else {
                    AuthUiState(checkingSession = false)
                }
            } catch (_: TimeoutCancellationException) {
                _uiState.value = AuthUiState(checkingSession = false)
            } catch (_: Exception) {
                _uiState.value = AuthUiState(checkingSession = false)
            }
        }
    }

    fun signIn(email: String, password: String, role: UserRole) {
        submit(email, password) {
            repository.signIn(email.trim(), password, role)
        }
    }

    fun signUp(email: String, password: String, role: UserRole) {
        submit(email, password) {
            repository.signUp(email.trim(), password, role)
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState()
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState(error = "Ingresa tu correo para enviar el enlace.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true, checkingSession = false)
            try {
                withTimeout(12000) {
                    withContext(Dispatchers.IO) {
                        repository.resetPassword(email.trim())
                    }
                }
                _uiState.value = AuthUiState(checkingSession = false, message = "Te enviamos un correo para restablecer tu contrasena.")
            } catch (e: Exception) {
                _uiState.value = AuthUiState(checkingSession = false, error = e.message ?: "No se pudo enviar el correo.")
            }
        }
    }

    fun consumeSession() {
        _uiState.value = _uiState.value.copy(session = null, loading = false, checkingSession = false)
    }

    private fun submit(
        email: String,
        password: String,
        action: suspend () -> UserSession
    ) {
        if (email.isBlank() || password.length < 6) {
            _uiState.value = AuthUiState(error = "Ingresa un correo y una contrasena de minimo 6 caracteres.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true, checkingSession = false)
            try {
                val session = withTimeout(15000) {
                    withContext(Dispatchers.IO) {
                        action()
                    }
                }
                _uiState.value = AuthUiState(session = session, checkingSession = false)
            } catch (_: TimeoutCancellationException) {
                _uiState.value = AuthUiState(
                    checkingSession = false,
                    error = "La conexion tardo demasiado. Revisa internet e intenta otra vez."
                )
            } catch (e: Exception) {
                _uiState.value = AuthUiState(checkingSession = false, error = e.message ?: "No se pudo completar la autenticacion.")
            }
        }
    }

    companion object {
        fun factory(repository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AuthViewModel(repository) as T
            }
    }
}
