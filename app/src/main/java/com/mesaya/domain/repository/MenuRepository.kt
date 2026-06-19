package com.mesaya.domain.repository

import com.mesaya.domain.model.Meal

interface MenuRepository {
    suspend fun searchMeals(query: String): List<Meal>
    suspend fun getMealsByCategory(category: String): List<Meal>
    suspend fun getCategories(): List<String>
}
