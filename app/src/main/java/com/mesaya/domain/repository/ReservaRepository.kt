package com.mesaya.domain.repository

import com.mesaya.domain.model.DetallePedido
import com.mesaya.domain.model.Reserva
import kotlinx.coroutines.flow.Flow

interface ReservaRepository {
    fun getReservas(): Flow<List<Reserva>>
    fun getReservasByEstado(estado: String): Flow<List<Reserva>>
    fun getReservaFlowById(id: Int): Flow<Reserva?>
    suspend fun getReservaById(id: Int): Reserva?
    suspend fun saveReserva(reserva: Reserva): Int
    suspend fun updateReserva(reserva: Reserva)
    suspend fun deleteReserva(reserva: Reserva)
    fun getDetallesByReserva(reservaId: Int): Flow<List<DetallePedido>>
    suspend fun saveDetalle(detalle: DetallePedido)
    suspend fun deleteDetallesByReserva(reservaId: Int)
}
