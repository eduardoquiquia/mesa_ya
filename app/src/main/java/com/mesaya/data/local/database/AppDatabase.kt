package com.mesaya.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mesaya.data.local.dao.DetallePedidoDao
import com.mesaya.data.local.dao.PlatoDao
import com.mesaya.data.local.dao.ReservaDao
import com.mesaya.data.local.entities.DetallePedidoEntity
import com.mesaya.data.local.entities.PlatoEntity
import com.mesaya.data.local.entities.ReservaEntity

@Database(
    entities = [ReservaEntity::class, PlatoEntity::class, DetallePedidoEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reservaDao(): ReservaDao
    abstract fun platoDao(): PlatoDao
    abstract fun detallePedidoDao(): DetallePedidoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mesaya_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
