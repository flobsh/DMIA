package com.flobsh.todo.authentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginForm(
        @SerialName("email")
        val email: String,
        @SerialName("password")
        val password: String
)
