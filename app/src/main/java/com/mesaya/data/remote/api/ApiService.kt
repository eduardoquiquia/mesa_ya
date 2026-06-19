package com.mesaya.data.remote.api

import com.mesaya.data.remote.dto.ReservaDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("reservas")
    suspend fun getReservas(): List<ReservaDto>

    @POST("reservas")
    suspend fun createReserva(@Body reserva: ReservaDto): ReservaDto

    @GET("reservas/{id}")
    suspend fun getReservaById(@Path("id") id: Int): ReservaDto
}
