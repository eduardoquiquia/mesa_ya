package com.mesaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mesaya.domain.model.AppUser
import com.mesaya.domain.model.UserRole
import com.mesaya.domain.repository.UserAdminRepository
import com.mesaya.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AdminUsersViewModel(
    private val repository: UserAdminRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<AppUser>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<AppUser>>> = _uiState

    init {
        viewModelScope.launch {
            repository.getUsers()
                .catch { e -> _uiState.value = UiState.Error(e.message ?: "No se pudo cargar usuarios") }
                .collect { users -> _uiState.value = UiState.Success(users) }
        }
    }

    fun makeAdmin(user: AppUser) {
        updateRole(user, UserRole.ADMIN)
    }

    fun makeCliente(user: AppUser) {
        updateRole(user, UserRole.CLIENTE)
    }

    private fun updateRole(user: AppUser, role: UserRole) {
        viewModelScope.launch {
            try {
                repository.updateUserRole(user.uid, role)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "No se pudo cambiar el rol")
            }
        }
    }

    companion object {
        fun factory(repository: UserAdminRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AdminUsersViewModel(repository) as T
            }
    }
}
