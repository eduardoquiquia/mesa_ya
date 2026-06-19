package com.mesaya

import android.app.Application
import com.mesaya.data.local.database.AppDatabase
import com.mesaya.data.remote.api.RetrofitModule
import com.mesaya.data.repository.MenuRepositoryImpl
import com.mesaya.data.repository.ReservaRepositoryImpl
import com.mesaya.domain.repository.MenuRepository
import com.mesaya.domain.repository.ReservaRepository

class MesaYaApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val reservaRepository: ReservaRepository by lazy {
        ReservaRepositoryImpl(
            reservaDao = database.reservaDao(),
            detallePedidoDao = database.detallePedidoDao(),
            api = RetrofitModule.apiService
        )
    }

    val menuRepository: MenuRepository by lazy {
        MenuRepositoryImpl(RetrofitModule.mealApiService)
    }
}
