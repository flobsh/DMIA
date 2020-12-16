package com.flobsh.todo.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flobsh.todo.R
import com.flobsh.todo.network.Api

class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Api.INSTANCE.getToken().isNullOrEmpty()) {
            setContentView(R.layout.activity_authentication)
        }
        else {
            setContentView(R.layout.activity_main)
        }
    }
}