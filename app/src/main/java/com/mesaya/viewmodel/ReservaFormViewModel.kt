package com.mesaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mesaya.domain.model.Reserva
import com.mesaya.domain.repository.ReservaRepository
import com.mesaya.utils.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReservaFormViewModel(
    private val repository: ReservaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Reserva?>>(UiState.Success(null))
    val uiState: StateFlow<UiState<Reserva?>> = _uiState

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _mesasOcupadas = MutableStateFlow<Set<Int>>(emptySet())
    val mesasOcupadas: StateFlow<Set<Int>> = _mesasOcupadas

    private var disponibilidadJob: Job? = null

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

    fun observarMesasOcupadas(fecha: Date?, hora: String, excludeReservaId: Int) {
        disponibilidadJob?.cancel()
        val cleanHora = hora.trim()
        if (fecha == null || cleanHora.isBlank()) {
            _mesasOcupadas.value = emptySet()
            return
        }

        val calendar = Calendar.getInstance().apply { time = fecha }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val fechaInicio = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val fechaFin = calendar.timeInMillis
        val slotKey = "${slotFormat.format(fecha)}_${cleanHora.filter { it.isDigit() }}"

        disponibilidadJob = viewModelScope.launch {
            repository.getMesasOcupadas(slotKey, fechaInicio, fechaFin, cleanHora, excludeReservaId)
                .collect { _mesasOcupadas.value = it }
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

private val slotFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
