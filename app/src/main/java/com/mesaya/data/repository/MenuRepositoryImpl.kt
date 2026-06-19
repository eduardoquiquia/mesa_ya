package com.mesaya.data.repository

import com.mesaya.data.remote.api.MealApiService
import com.mesaya.domain.model.Meal
import com.mesaya.domain.repository.MenuRepository

class MenuRepositoryImpl(
    private val mealApiService: MealApiService
) : MenuRepository {

    override suspend fun searchMeals(query: String): List<Meal> {
        val response = mealApiService.searchMeals(query)
        return response.meals?.map { dto ->
            Meal(
                mealId = dto.idMeal,
                nombre = dto.strMeal,
                categoria = dto.strCategory ?: "",
                imagenUrl = dto.strMealThumb ?: "",
                precio = calcularPrecio(dto.idMeal)
            )
        } ?: emptyList()
    }

    override suspend fun getMealsByCategory(category: String): List<Meal> {
        val response = mealApiService.getMealsByCategory(category)
        return response.meals?.map { dto ->
            Meal(
                mealId = dto.idMeal,
                nombre = dto.strMeal,
                categoria = category,
                imagenUrl = dto.strMealThumb ?: "",
                precio = calcularPrecio(dto.idMeal)
            )
        } ?: emptyList()
    }

    override suspend fun getCategories(): List<String> {
        val response = mealApiService.getCategories()
        return response.categories?.map { it.strCategory } ?: emptyList()
    }

    private fun calcularPrecio(mealId: String): Double {
        val id = mealId.toLongOrNull() ?: 0L
        return (id % 30 + 10).toDouble() + 0.50
    }
}
