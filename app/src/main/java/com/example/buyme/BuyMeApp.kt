package com.example.buyme

import android.app.Application
import com.example.buyme.data.AppDatabase

object Graph {
    lateinit var db: AppDatabase
        private set

    fun provide(app: Application) {
        db = AppDatabase.build(app.applicationContext)
    }
}

class BuyMeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}
