package com.mesaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mesaya.domain.model.DetallePedido
import com.mesaya.domain.model.EstadoReserva
import com.mesaya.domain.model.Reserva
import com.mesaya.domain.repository.ReservaRepository
import com.mesaya.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ReservaDetailViewModel(
    private val repository: ReservaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Reserva>>(UiState.Loading)
    val uiState: StateFlow<UiState<Reserva>> = _uiState

    private val _detalles = MutableStateFlow<List<DetallePedido>>(emptyList())
    val detalles: StateFlow<List<DetallePedido>> = _detalles

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total

    fun loadReserva(id: Int) {
        viewModelScope.launch {
            repository.getReservaFlowById(id)
                .catch { e -> _uiState.value = UiState.Error(e.message ?: "Error al cargar") }
                .collect { reserva ->
                    if (reserva != null) {
                        _uiState.value = UiState.Success(reserva)
                    } else {
                        _uiState.value = UiState.Error("Reserva no encontrada")
                    }
                }
        }
        viewModelScope.launch {
            repository.getDetallesByReserva(id)
                .catch { /* silencioso */ }
                .collect { detalles ->
                    _detalles.value = detalles
                    val nuevoTotal = detalles.sumOf { it.subtotal }
                    _total.value = nuevoTotal
                    val reservaActual = (_uiState.value as? UiState.Success)?.data
                    if (reservaActual != null && reservaActual.total != nuevoTotal) {
                        repository.updateReserva(reservaActual.copy(total = nuevoTotal))
                    }
                }
        }
    }

    fun cambiarEstado(reservaId: Int, nuevoEstado: EstadoReserva) {
        viewModelScope.launch {
            try {
                val reserva = repository.getReservaById(reservaId) ?: return@launch
                repository.updateReserva(reserva.copy(estado = nuevoEstado.value))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cambiar estado")
            }
        }
    }

    fun eliminarDetalle(detalle: DetallePedido) {
        viewModelScope.launch {
            try {
                repository.deleteDetallesByReserva(detalle.reservaId)
                val restantes = _detalles.value.filter { it.id != detalle.id }
                for (item in restantes) {
                    repository.saveDetalle(item)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar plato")
            }
        }
    }

    companion object {
        fun factory(repository: ReservaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReservaDetailViewModel(repository) as T
            }
    }
}
