package com.flobsh.todo.tasklist

import java.io.Serializable
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Task (
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String = "This is a description") : Serializable {}