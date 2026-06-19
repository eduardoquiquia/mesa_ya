package com.mesaya.data.repository

import com.mesaya.data.local.dao.DetallePedidoDao
import com.mesaya.data.local.dao.ReservaDao
import com.mesaya.data.local.entities.DetallePedidoEntity
import com.mesaya.data.local.entities.ReservaEntity
import com.mesaya.data.remote.api.ApiService
import com.mesaya.domain.model.DetallePedido
import com.mesaya.domain.model.Reserva
import com.mesaya.domain.repository.ReservaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class ReservaRepositoryImpl(
    private val reservaDao: ReservaDao,
    private val detallePedidoDao: DetallePedidoDao,
    private val api: ApiService
) : ReservaRepository {

    override fun getReservas(): Flow<List<Reserva>> =
        reservaDao.getAllReservas().map { list -> list.map { it.toDomain() } }

    override fun getReservasByEstado(estado: String): Flow<List<Reserva>> =
        reservaDao.getReservasByEstado(estado).map { list -> list.map { it.toDomain() } }

    override fun getReservaFlowById(id: Int): Flow<Reserva?> =
        reservaDao.getReservaFlowById(id).map { it?.toDomain() }

    override suspend fun getReservaById(id: Int): Reserva? =
        reservaDao.getReservaById(id)?.toDomain()

    override suspend fun saveReserva(reserva: Reserva): Int =
        reservaDao.insertReserva(reserva.toEntity()).toInt()

    override suspend fun updateReserva(reserva: Reserva) =
        reservaDao.updateReserva(reserva.toEntity())

    override suspend fun deleteReserva(reserva: Reserva) =
        reservaDao.deleteReserva(reserva.toEntity())

    override fun getDetallesByReserva(reservaId: Int): Flow<List<DetallePedido>> =
        detallePedidoDao.getDetallesByReserva(reservaId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveDetalle(detalle: DetallePedido) {
        detallePedidoDao.insertDetalle(detalle.toEntity())
    }

    override suspend fun deleteDetallesByReserva(reservaId: Int) =
        detallePedidoDao.deleteDetallesByReserva(reservaId)
}

private fun ReservaEntity.toDomain() = Reserva(
    id = id,
    clienteNombre = clienteNombre,
    fecha = Date(fecha),
    hora = hora,
    numeroPersonas = numeroPersonas,
    mesaId = mesaId,
    estado = estado,
    avisoLlegada = avisoLlegada,
    total = total
)

private fun Reserva.toEntity() = ReservaEntity(
    id = id,
    clienteNombre = clienteNombre,
    fecha = fecha.time,
    hora = hora,
    numeroPersonas = numeroPersonas,
    mesaId = mesaId,
    estado = estado,
    avisoLlegada = avisoLlegada,
    total = total
)

private fun DetallePedidoEntity.toDomain() = DetallePedido(
    id = id,
    reservaId = reservaId,
    mealId = mealId,
    nombre = nombre,
    imagenUrl = imagenUrl,
    precio = precio,
    cantidad = cantidad,
    subtotal = subtotal,
    platoId = platoId
)

private fun DetallePedido.toEntity() = DetallePedidoEntity(
    id = id,
    reservaId = reservaId,
    mealId = mealId,
    nombre = nombre,
    imagenUrl = imagenUrl,
    precio = precio,
    cantidad = cantidad,
    subtotal = subtotal,
    platoId = platoId
)
