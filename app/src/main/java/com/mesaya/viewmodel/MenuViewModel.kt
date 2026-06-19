package com.mesaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mesaya.domain.model.DetallePedido
import com.mesaya.domain.model.Meal
import com.mesaya.domain.repository.MenuRepository
import com.mesaya.domain.repository.ReservaRepository
import com.mesaya.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MenuViewModel(
    private val reservaRepository: ReservaRepository,
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _mealsUiState = MutableStateFlow<UiState<List<Meal>>>(UiState.Loading)
    val mealsUiState: StateFlow<UiState<List<Meal>>> = _mealsUiState

    private val _pedidoItems = MutableStateFlow<List<DetallePedido>>(emptyList())
    val pedidoItems: StateFlow<List<DetallePedido>> = _pedidoItems

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private var reservaId: Int = 0

    fun initialize(reservaId: Int) {
        this.reservaId = reservaId
        loadCategories()
        loadInitialMeals()
        observePedido(reservaId)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = menuRepository.getCategories()
            } catch (_: Exception) {}
        }
    }

    private fun loadInitialMeals() {
        viewModelScope.launch {
            _mealsUiState.value = UiState.Loading
            try {
                val meals = menuRepository.getMealsByCategory("Chicken")
                _mealsUiState.value = UiState.Success(meals)
            } catch (e: Exception) {
                _mealsUiState.value = UiState.Error(e.message ?: "Error al cargar el menú")
            }
        }
    }

    fun searchMeals(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadInitialMeals()
            return
        }
        viewModelScope.launch {
            _mealsUiState.value = UiState.Loading
            try {
                val meals = menuRepository.searchMeals(query)
                _mealsUiState.value = UiState.Success(meals)
            } catch (e: Exception) {
                _mealsUiState.value = UiState.Error(e.message ?: "Error en la búsqueda")
            }
        }
    }

    fun loadByCategory(category: String) {
        viewModelScope.launch {
            _mealsUiState.value = UiState.Loading
            try {
                val meals = menuRepository.getMealsByCategory(category)
                _mealsUiState.value = UiState.Success(meals)
            } catch (e: Exception) {
                _mealsUiState.value = UiState.Error(e.message ?: "Error al cargar categoría")
            }
        }
    }

    private fun observePedido(reservaId: Int) {
        viewModelScope.launch {
            reservaRepository.getDetallesByReserva(reservaId)
                .catch { /* silencioso */ }
                .collect { _pedidoItems.value = it }
        }
    }

    fun addToPedido(meal: Meal, cantidad: Int) {
        viewModelScope.launch {
            try {
                val existing = _pedidoItems.value.find { it.mealId == meal.mealId }
                if (existing != null) {
                    val nuevaCantidad = existing.cantidad + cantidad
                    reservaRepository.saveDetalle(
                        existing.copy(
                            cantidad = nuevaCantidad,
                            subtotal = nuevaCantidad * meal.precio
                        )
                    )
                } else {
                    reservaRepository.saveDetalle(
                        DetallePedido(
                            reservaId = reservaId,
                            mealId = meal.mealId,
                            nombre = meal.nombre,
                            imagenUrl = meal.imagenUrl,
                            precio = meal.precio,
                            cantidad = cantidad,
                            subtotal = meal.precio * cantidad
                        )
                    )
                }
            } catch (e: Exception) {
                // Error silencioso; el UI puede mostrar snackbar si lo necesita
            }
        }
    }

    companion object {
        fun factory(
            reservaRepository: ReservaRepository,
            menuRepository: MenuRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MenuViewModel(reservaRepository, menuRepository) as T
            }
    }
}
