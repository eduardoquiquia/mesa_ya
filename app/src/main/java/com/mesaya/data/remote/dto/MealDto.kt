package com.mesaya.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MealDto(
    @SerializedName("idMeal") val idMeal: String,
    @SerializedName("strMeal") val strMeal: String,
    @SerializedName("strCategory") val strCategory: String?,
    @SerializedName("strMealThumb") val strMealThumb: String?
)

data class MealsResponseDto(
    @SerializedName("meals") val meals: List<MealDto>?
)

data class CategoryDto(
    @SerializedName("idCategory") val idCategory: String,
    @SerializedName("strCategory") val strCategory: String,
    @SerializedName("strCategoryThumb") val strCategoryThumb: String?
)

data class CategoriesResponseDto(
    @SerializedName("categories") val categories: List<CategoryDto>?
)
