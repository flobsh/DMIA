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
            val response = repository.loadTasks()
            if (response.isSuccessful) {
                _taskList.value = response.body()
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val response = repository.deleteTask(task)
            if (response.isSuccessful) {
                val editableList = _taskList.value.orEmpty().toMutableList()
                editableList.remove(task)
                _taskList.value = editableList
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            val response = repository.addTask(task)
            if (response.isSuccessful) {
                val addedTask = response.body()
                val editableList = _taskList.value.orEmpty().toMutableList()
                editableList.add(addedTask!!)
                _taskList.value = editableList
            }
        }
    }

    fun editTask(task: Task) {
        viewModelScope.launch {
            val response = repository.editTask(task)
            if (response.isSuccessful) {
                val editedTask = response.body()
                val editableList = _taskList.value.orEmpty().toMutableList()
                val position = editableList.indexOfFirst { editedTask!!.id == it.id }
                editableList[position] = editedTask!!
                _taskList.value = editableList
            }
        }
    }
}