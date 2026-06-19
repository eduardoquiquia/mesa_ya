package com.mesaya.data.remote.api

import com.mesaya.data.remote.dto.CategoriesResponseDto
import com.mesaya.data.remote.dto.MealsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {
    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): MealsResponseDto

    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): MealsResponseDto

    @GET("categories.php")
    suspend fun getCategories(): CategoriesResponseDto
}
