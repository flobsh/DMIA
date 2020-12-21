package com.flobsh.todo.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flobsh.todo.tasklist.Task
import com.flobsh.todo.tasklist.TaskListViewModel
import retrofit2.Response

class TasksRepository {
    private val tasksWebService = Api.INSTANCE.tasksWebService

    // Ces deux variables encapsulent la même donnée:
    // [_taskList] est modifiable mais privée donc inaccessible à l'extérieur de cette classe
    private val _taskList = MutableLiveData<List<Task>>()
    // [taskList] est publique mais non-modifiable:
    // On pourra seulement l'observer (s'y abonner) depuis d'autres classes
    public val taskList: LiveData<List<Task>> = _taskList

    suspend fun loadTasks(): Response<List<Task>> {
        return tasksWebService.getTasks()
    }

    suspend fun addTask(task: Task) : Response<Task> {
        return tasksWebService.createTask(task)
    }

    suspend fun deleteTask(task: Task) : Response<String> {
        return tasksWebService.deleteTask(task.id)
    }

    suspend fun editTask(task: Task) : Response<Task> {
        return tasksWebService.updateTask(task)
    }
}