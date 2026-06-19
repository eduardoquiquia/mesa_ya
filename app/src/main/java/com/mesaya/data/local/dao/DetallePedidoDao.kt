package com.mesaya.data.local.dao

import androidx.room.*
import com.mesaya.data.local.entities.DetallePedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetallePedidoDao {
    @Query("SELECT * FROM detalles_pedido WHERE reservaId = :reservaId")
    fun getDetallesByReserva(reservaId: Int): Flow<List<DetallePedidoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetalle(detalle: DetallePedidoEntity): Long

    @Update
    suspend fun updateDetalle(detalle: DetallePedidoEntity)

    @Delete
    suspend fun deleteDetalle(detalle: DetallePedidoEntity)

    @Query("DELETE FROM detalles_pedido WHERE reservaId = :reservaId")
    suspend fun deleteDetallesByReserva(reservaId: Int)
}
