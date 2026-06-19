package com.mesaya.data.remote.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitModule {
    private const val MESAYA_BASE_URL = "https://api.mesaya.com/"
    private const val THEMEALDB_BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MESAYA_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val mealApiService: MealApiService by lazy {
        Retrofit.Builder()
            .baseUrl(THEMEALDB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}
