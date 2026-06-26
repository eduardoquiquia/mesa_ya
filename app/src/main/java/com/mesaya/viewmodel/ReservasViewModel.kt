package com.mesaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mesaya.domain.model.EstadoReserva
import com.mesaya.domain.model.Reserva
import com.mesaya.domain.repository.ReservaRepository
import com.mesaya.utils.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ReservasViewModel(
    private val repository: ReservaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Reserva>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Reserva>>> = _uiState

    private val _selectedEstado = MutableStateFlow<String?>(null)
    val selectedEstado: StateFlow<String?> = _selectedEstado

    init {
        viewModelScope.launch {
            _selectedEstado.flatMapLatest { estado ->
                if (estado == null) repository.getReservas()
                else repository.getReservasByEstado(estado)
            }.catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Error al cargar reservas")
            }.collect { reservas ->
                _uiState.value = UiState.Success(reservas)
            }
        }
    }

    fun setFilter(estado: String?) {
        _selectedEstado.value = estado
    }

    fun deleteReserva(reserva: Reserva) {
        viewModelScope.launch {
            try {
                repository.deleteDetallesByReserva(reserva.id)
                repository.deleteReserva(reserva)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar")
            }
        }
    }

    fun cambiarEstado(reserva: Reserva, nuevoEstado: EstadoReserva) {
        viewModelScope.launch {
            try {
                repository.updateReserva(reserva.copy(estado = nuevoEstado.value))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cambiar estado")
            }
        }
    }

    companion object {
        fun factory(repository: ReservaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReservasViewModel(repository) as T
            }
    }
}
