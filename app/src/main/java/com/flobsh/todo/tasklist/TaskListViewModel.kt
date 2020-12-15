package com.flobsh.todo.tasklist

import androidx.lifecycle.*
import com.flobsh.todo.network.TasksRepository
import kotlinx.coroutines.launch

class TaskListViewModel : ViewModel() {
    private val repository = TasksRepository()
    private val _taskList = MutableLiveData<List<Task>>()
    public val taskList: LiveData<List<Task>> = _taskList

    fun loadTasks() {
        viewModelScope.launch {
            _taskList.value = repository.loadTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            if (repository.deleteTask(task)) {
                val editableList = _taskList.value.orEmpty().toMutableList()
                editableList.remove(task)
                _taskList.value = editableList
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            val createdTask = repository.addTask(task)
            if (createdTask != null) {
                val editableList = _taskList.value.orEmpty().toMutableList()
                editableList.add(createdTask)
                _taskList.value = editableList
            }
        }
    }

    fun editTask(task: Task) {}
}