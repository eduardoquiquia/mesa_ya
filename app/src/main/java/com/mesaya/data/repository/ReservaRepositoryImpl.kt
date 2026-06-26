package com.mesaya.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.mesaya.data.local.dao.DetallePedidoDao
import com.mesaya.data.local.dao.ReservaDao
import com.mesaya.data.local.entities.DetallePedidoEntity
import com.mesaya.data.local.entities.ReservaEntity
import com.mesaya.data.remote.api.ApiService
import com.mesaya.domain.model.DetallePedido
import com.mesaya.domain.model.EstadoReserva
import com.mesaya.domain.model.Reserva
import com.mesaya.domain.repository.ReservaRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReservaRepositoryImpl(
    private val context: Context,
    private val reservaDao: ReservaDao,
    private val detallePedidoDao: DetallePedidoDao,
    private val api: ApiService
) : ReservaRepository {

    override fun getReservas(): Flow<List<Reserva>> {
        val userId = currentUserId()
        return if (isAdminSession()) {
            getAdminReservasRemote()
        } else if (userId.isBlank()) {
            reservaDao.getAllReservas().map { list -> list.map { it.toDomain() } }
        } else {
            reservaDao.getReservasByUser(userId).map { list -> list.map { it.toDomain() } }
        }
    }

    override fun getReservasByEstado(estado: String): Flow<List<Reserva>> {
        val userId = currentUserId()
        return if (isAdminSession()) {
            getAdminReservasRemote(estado)
        } else if (userId.isBlank()) {
            reservaDao.getReservasByEstado(estado).map { list -> list.map { it.toDomain() } }
        } else {
            reservaDao.getReservasByUserAndEstado(userId, estado).map { list -> list.map { it.toDomain() } }
        }
    }

    override fun getMesasOcupadas(
        slotKey: String,
        fechaInicio: Long,
        fechaFin: Long,
        hora: String,
        excludeReservaId: Int
    ): Flow<Set<Int>> {
        val uid = currentUserId()
        return if (uid.isBlank()) {
            reservaDao.getReservasByFechaHora(fechaInicio, fechaFin, hora).map { reservas ->
                reservas
                    .filter { it.id != excludeReservaId && it.estado != EstadoReserva.COMPLETADA.value }
                    .map { it.mesaId }
                    .toSet()
            }
        } else {
            getMesasOcupadasRemote(slotKey, fechaInicio, fechaFin, hora, excludeReservaId)
        }
    }

    override fun getReservaFlowById(id: Int): Flow<Reserva?> =
        reservaDao.getReservaFlowById(id).map { it?.toDomain() }

    override suspend fun getReservaById(id: Int): Reserva? =
        reservaDao.getReservaById(id)?.toDomain()

    override suspend fun saveReserva(reserva: Reserva): Int {
        val scopedReserva = reserva.withCurrentUser()
        val id = reservaDao.insertReserva(scopedReserva.toEntity()).toInt()
        syncReserva(scopedReserva.copy(id = id))
        return id
    }

    override suspend fun updateReserva(reserva: Reserva) {
        val previous = if (reserva.id == 0) null else getReservaById(reserva.id)
        val scopedReserva = reserva.withCurrentUser()
        reservaDao.updateReserva(scopedReserva.toEntity())
        syncReserva(scopedReserva, previous)
    }

    override suspend fun deleteReserva(reserva: Reserva) {
        reservaDao.deleteReserva(reserva.toEntity())
        deleteReservaRemote(reserva)
    }

    override fun getDetallesByReserva(reservaId: Int): Flow<List<DetallePedido>> =
        detallePedidoDao.getDetallesByReserva(reservaId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveDetalle(detalle: DetallePedido) {
        detallePedidoDao.insertDetalle(detalle.toEntity())
        syncDetalle(detalle)
    }

    override suspend fun deleteDetallesByReserva(reservaId: Int) {
        detallePedidoDao.deleteDetallesByReserva(reservaId)
        deleteDetallesRemote(reservaId)
    }

    private fun currentUserId(): String =
        try {
            FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        } catch (_: Exception) {
            ""
        }

    private fun isAdminSession(): Boolean =
        context.getSharedPreferences("mesaya_session", Context.MODE_PRIVATE)
            .getString("role", "") == "admin"

    private fun Reserva.withCurrentUser(): Reserva {
        val uid = currentUserId()
        return if (userId.isBlank() && uid.isNotBlank()) copy(userId = uid) else this
    }

    private suspend fun syncReserva(reserva: Reserva, previous: Reserva? = null) {
        val uid = reserva.userId.ifBlank { currentUserId() }
        if (uid.isBlank() || reserva.id == 0) return
        runCatching {
            if (previous != null && previous.occupationDocId() != reserva.occupationDocId()) {
                deleteMesaOcupada(previous)
            }
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("reservas")
                .document(reserva.id.toString())
                .set(reserva.toFirestoreMap(uid))
                .await()
            FirebaseFirestore.getInstance()
                .collection("reservas_admin")
                .document(reserva.id.toString())
                .set(reserva.toFirestoreMap(uid))
                .await()
            syncMesaOcupada(reserva.copy(userId = uid))
        }
    }

    private suspend fun deleteReservaRemote(reserva: Reserva) {
        val uid = reserva.userId.ifBlank { currentUserId() }
        if (uid.isBlank() || reserva.id == 0) return
        runCatching {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("reservas")
                .document(reserva.id.toString())
                .delete()
                .await()
            FirebaseFirestore.getInstance()
                .collection("reservas_admin")
                .document(reserva.id.toString())
                .delete()
                .await()
            deleteMesaOcupada(reserva)
        }
    }

    private suspend fun syncDetalle(detalle: DetallePedido) {
        val uid = currentUserId()
        if (uid.isBlank() || detalle.reservaId == 0) return
        runCatching {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("reservas")
                .document(detalle.reservaId.toString())
                .collection("pedido")
                .document(detalle.mealId.ifBlank { detalle.id.toString() })
                .set(detalle.toFirestoreMap())
                .await()
            FirebaseFirestore.getInstance()
                .collection("reservas_admin")
                .document(detalle.reservaId.toString())
                .collection("pedido")
                .document(detalle.mealId.ifBlank { detalle.id.toString() })
                .set(detalle.toFirestoreMap())
                .await()
        }
    }

    private suspend fun deleteDetallesRemote(reservaId: Int) {
        val reserva = getReservaById(reservaId)
        val uid = reserva?.userId?.ifBlank { currentUserId() } ?: currentUserId()
        runCatching {
            if (uid.isNotBlank()) {
                val pedido = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("reservas")
                    .document(reservaId.toString())
                    .collection("pedido")
                    .get()
                    .await()
                pedido.documents.forEach { it.reference.delete().await() }
            }

            val adminPedido = FirebaseFirestore.getInstance()
                .collection("reservas_admin")
                .document(reservaId.toString())
                .collection("pedido")
                .get()
                .await()
            adminPedido.documents.forEach { it.reference.delete().await() }
        }
    }

    private fun getAdminReservasRemote(estado: String? = null): Flow<List<Reserva>> = callbackFlow {
        var query: com.google.firebase.firestore.Query = FirebaseFirestore.getInstance()
            .collection("reservas_admin")

        if (estado != null) {
            query = query.whereEqualTo("estado", estado)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val reservas = snapshot?.documents
                ?.mapNotNull { it.toReservaOrNull() }
                ?.sortedByDescending { it.fecha.time }
                .orEmpty()
            trySend(reservas)
        }

        awaitClose { listener.remove() }
    }

    private fun getMesasOcupadasRemote(
        slotKey: String,
        fechaInicio: Long,
        fechaFin: Long,
        hora: String,
        excludeReservaId: Int
    ): Flow<Set<Int>> = callbackFlow {
        val listener = FirebaseFirestore.getInstance()
            .collection("mesa_ocupacion")
            .whereEqualTo("slotKey", slotKey)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptySet())
                    return@addSnapshotListener
                }
                val mesas = snapshot?.documents
                    ?.filter { doc ->
                        val reservaId = doc.getLong("reservaId")?.toInt() ?: 0
                        val estado = doc.getString("estado").orEmpty()
                        val fecha = doc.getLong("fecha") ?: 0L
                        val docHora = doc.getString("hora").orEmpty()
                        reservaId != excludeReservaId &&
                            estado != EstadoReserva.COMPLETADA.value &&
                            fecha in fechaInicio..fechaFin &&
                            docHora == hora
                    }
                    ?.mapNotNull { it.getLong("mesaId")?.toInt() }
                    ?.toSet()
                    .orEmpty()
                trySend(mesas)
            }

        awaitClose { listener.remove() }
    }

    private suspend fun syncMesaOcupada(reserva: Reserva) {
        val ref = FirebaseFirestore.getInstance()
            .collection("mesa_ocupacion")
            .document(reserva.occupationDocId())
        if (reserva.estado == EstadoReserva.COMPLETADA.value) {
            ref.delete().await()
        } else {
            ref.set(reserva.toMesaOcupadaMap()).await()
        }
    }

    private suspend fun deleteMesaOcupada(reserva: Reserva) {
        FirebaseFirestore.getInstance()
            .collection("mesa_ocupacion")
            .document(reserva.occupationDocId())
            .delete()
            .await()
    }
}

private val slotDateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

private fun Reserva.slotKey(): String =
    "${slotDateFormat.format(fecha)}_${hora.filter { it.isDigit() }}"

private fun Reserva.occupationDocId(): String = "${slotKey()}_$mesaId"

private fun DocumentSnapshot.toReservaOrNull(): Reserva? {
    val id = getLong("id")?.toInt() ?: this.id.toIntOrNull() ?: return null
    val fecha = getLong("fecha") ?: return null
    return Reserva(
        id = id,
        userId = getString("userId").orEmpty(),
        clienteNombre = getString("clienteNombre").orEmpty(),
        fecha = Date(fecha),
        hora = getString("hora").orEmpty(),
        numeroPersonas = getLong("numeroPersonas")?.toInt() ?: 1,
        mesaId = getLong("mesaId")?.toInt() ?: 1,
        estado = getString("estado") ?: EstadoReserva.PENDIENTE.value,
        avisoLlegada = getBoolean("avisoLlegada") ?: false,
        total = getDouble("total") ?: 0.0,
        metodoPago = getString("metodoPago") ?: "pendiente"
    )
}

private fun ReservaEntity.toDomain() = Reserva(
    id = id,
    userId = userId,
    clienteNombre = clienteNombre,
    fecha = Date(fecha),
    hora = hora,
    numeroPersonas = numeroPersonas,
    mesaId = mesaId,
    estado = estado,
    avisoLlegada = avisoLlegada,
    total = total,
    metodoPago = metodoPago
)

private fun Reserva.toEntity() = ReservaEntity(
    id = id,
    userId = userId,
    clienteNombre = clienteNombre,
    fecha = fecha.time,
    hora = hora,
    numeroPersonas = numeroPersonas,
    mesaId = mesaId,
    estado = estado,
    avisoLlegada = avisoLlegada,
    total = total,
    metodoPago = metodoPago
)

private fun Reserva.toFirestoreMap(userId: String) = mapOf(
    "id" to id,
    "userId" to userId,
    "clienteNombre" to clienteNombre,
    "fecha" to fecha.time,
    "hora" to hora,
    "numeroPersonas" to numeroPersonas,
    "mesaId" to mesaId,
    "estado" to estado,
    "avisoLlegada" to avisoLlegada,
    "total" to total,
    "metodoPago" to metodoPago
)

private fun Reserva.toMesaOcupadaMap() = mapOf(
    "slotKey" to slotKey(),
    "mesaId" to mesaId,
    "reservaId" to id,
    "userId" to userId,
    "fecha" to fecha.time,
    "hora" to hora,
    "estado" to estado
)

private fun DetallePedido.toFirestoreMap() = mapOf(
    "id" to id,
    "reservaId" to reservaId,
    "mealId" to mealId,
    "nombre" to nombre,
    "imagenUrl" to imagenUrl,
    "precio" to precio,
    "cantidad" to cantidad,
    "subtotal" to subtotal,
    "platoId" to platoId
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
