package com.flobsh.todo.tasklist

import java.io.Serializable

data class Task (val id: String, val title: String, val description: String = "This is a description") : Serializable {}