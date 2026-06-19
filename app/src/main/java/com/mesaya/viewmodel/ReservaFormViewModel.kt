package com.mesaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mesaya.domain.model.Reserva
import com.mesaya.domain.repository.ReservaRepository
import com.mesaya.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReservaFormViewModel(
    private val repository: ReservaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Reserva?>>(UiState.Success(null))
    val uiState: StateFlow<UiState<Reserva?>> = _uiState

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun loadReserva(id: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val reserva = repository.getReservaById(id)
                _uiState.value = UiState.Success(reserva)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cargar reserva")
            }
        }
    }

    fun saveReserva(reserva: Reserva) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                if (reserva.id == 0) {
                    repository.saveReserva(reserva)
                } else {
                    repository.updateReserva(reserva)
                }
                _saveSuccess.value = true
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al guardar reserva")
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    companion object {
        fun factory(repository: ReservaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReservaFormViewModel(repository) as T
            }
    }
}
