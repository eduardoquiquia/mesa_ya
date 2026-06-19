package com.mesaya.data.local.dao

import androidx.room.*
import com.mesaya.data.local.entities.PlatoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlatoDao {
    @Query("SELECT * FROM platos")
    fun getAllPlatos(): Flow<List<PlatoEntity>>

    @Query("SELECT * FROM platos WHERE id = :id")
    suspend fun getPlatoById(id: Int): PlatoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlato(plato: PlatoEntity): Long

    @Delete
    suspend fun deletePlato(plato: PlatoEntity)
}
