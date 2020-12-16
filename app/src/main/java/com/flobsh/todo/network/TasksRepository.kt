package com.flobsh.todo.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flobsh.todo.tasklist.Task
import com.flobsh.todo.tasklist.TaskListViewModel

class TasksRepository {
    private val tasksWebService = Api.INSTANCE.tasksWebService

    // Ces deux variables encapsulent la même donnée:
    // [_taskList] est modifiable mais privée donc inaccessible à l'extérieur de cette classe
    private val _taskList = MutableLiveData<List<Task>>()
    // [taskList] est publique mais non-modifiable:
    // On pourra seulement l'observer (s'y abonner) depuis d'autres classes
    public val taskList: LiveData<List<Task>> = _taskList

    suspend fun loadTasks(): List<Task>? {
        val response = tasksWebService.getTasks()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun addTask(task: Task) : Task? {
        val response = tasksWebService.createTask(task)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun deleteTask(task: Task) : Boolean {
        val response  = tasksWebService.deleteTask(task.id)
        return response.isSuccessful
    }

    suspend fun updateTask(task: Task) {
        // Call HTTP (opération longue):
        val tasksResponse = tasksWebService.updateTask(task)
        // À la ligne suivante, on a reçu la réponse de l'API:
        if (tasksResponse.isSuccessful) {
            val editableList = _taskList.value.orEmpty().toMutableList()
            val updatedTask = tasksResponse.body()
            val position = editableList.indexOfFirst { updatedTask!!.id == it.id }
            editableList[position] = updatedTask!!
            _taskList.value = editableList
        }
    }
}