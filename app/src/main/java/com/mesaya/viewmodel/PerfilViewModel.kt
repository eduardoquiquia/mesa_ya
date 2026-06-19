package com.mesaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mesaya.domain.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class PerfilStats(
    val totalReservas: Int = 0,
    val reservasAtendidas: Int = 0,
    val reservasCanceladas: Int = 0
)

class PerfilViewModel(
    private val repository: ReservaRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(PerfilStats())
    val stats: StateFlow<PerfilStats> = _stats

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
                        reservasAtendidas = reservas.count { it.estado == "atendida" },
                        reservasCanceladas = reservas.count { it.estado == "cancelada" }
                    )
                }
        }
    }

    companion object {
        fun factory(repository: ReservaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PerfilViewModel(repository) as T
            }
    }
}
