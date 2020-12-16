package com.flobsh.todo.network

import android.app.Application

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        Api.INSTANCE = Api(this)
    }
}