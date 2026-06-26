package com.mesaya.data.local.dao

import androidx.room.*
import com.mesaya.data.local.entities.ReservaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {
    @Query("SELECT * FROM reservas ORDER BY fecha DESC")
    fun getAllReservas(): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE userId = :userId ORDER BY fecha DESC")
    fun getReservasByUser(userId: String): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE estado = :estado ORDER BY fecha DESC")
    fun getReservasByEstado(estado: String): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE userId = :userId AND estado = :estado ORDER BY fecha DESC")
    fun getReservasByUserAndEstado(userId: String, estado: String): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE fecha BETWEEN :fechaInicio AND :fechaFin AND hora = :hora ORDER BY mesaId")
    fun getReservasByFechaHora(fechaInicio: Long, fechaFin: Long, hora: String): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE id = :id")
    suspend fun getReservaById(id: Int): ReservaEntity?

    @Query("SELECT * FROM reservas WHERE id = :id")
    fun getReservaFlowById(id: Int): Flow<ReservaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReserva(reserva: ReservaEntity): Long

    @Update
    suspend fun updateReserva(reserva: ReservaEntity)

    @Delete
    suspend fun deleteReserva(reserva: ReservaEntity)
}
