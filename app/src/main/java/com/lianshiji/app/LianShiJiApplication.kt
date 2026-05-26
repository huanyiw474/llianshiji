package com.lianshiji.app

import android.app.Application
import com.lianshiji.app.data.local.LianShiJiDatabase
import com.lianshiji.app.data.repository.FitnessRepository

class LianShiJiApplication : Application() {
    lateinit var repository: FitnessRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = LianShiJiDatabase.getDatabase(this)
        repository = FitnessRepository(database)
    }
}
